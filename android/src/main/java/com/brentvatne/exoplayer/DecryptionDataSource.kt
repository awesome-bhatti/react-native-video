package com.brentvatne.exoplayer

import android.net.Uri
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.TransferListener
import java.io.IOException
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.math.min

class DecryptionDataSource(
    private val upstream: DataSource,
    private val keyProvider: EncryptionKeyProvider
) : DataSource {

    private var decryptedData: ByteArray? = null
    private var bytesReadSoFar: Int = 0

    @Throws(IOException::class)
    override fun open(dataSpec: DataSpec): Long {
        // Open the upstream data source.
        val length = upstream.open(dataSpec)
        if (length <= 0) {
            throw IOException("Invalid content length")
        }

        if (dataSpec.uri.path?.endsWith(".m3u8") == true) {
            // Read the encrypted data into memory.
            val encryptedBuffer = ByteArray(length.toInt().coerceAtLeast(0))
            var totalRead = 0
            while (totalRead < encryptedBuffer.size) {
                val read = upstream.read(encryptedBuffer, totalRead, encryptedBuffer.size - totalRead)
                if (read == -1) break
                totalRead += read
            }

            // Retrieve the current encryption keys from the provider.
            val encryptionKeys = keyProvider.getEncryptionKeys()

            // Decrypt using AES/CBC/PKCS5Padding.
            try {
                val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                val secretKeySpec = SecretKeySpec(encryptionKeys.key, "AES")
                val ivSpec = IvParameterSpec(encryptionKeys.iv)
                cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec)
                decryptedData = cipher.doFinal(encryptedBuffer)
                bytesReadSoFar = 0
            } catch (e: Exception) {
                throw IOException("Error decrypting data: ${e.message}", e)
            }

            return decryptedData?.size?.toLong() ?: 0L
        } else {
            return length
        }
    }

    @Throws(IOException::class)
    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        val data = decryptedData ?: return -1
        if (bytesReadSoFar >= data.size) {
            return -1 // End of stream
        }

        val bytesRemaining = data.size - bytesReadSoFar
        val bytesToRead = min(length, bytesRemaining)
        System.arraycopy(data, bytesReadSoFar, buffer, offset, bytesToRead)
        bytesReadSoFar += bytesToRead
        return bytesToRead
    }

    override fun getUri(): Uri? = upstream.uri

    @Throws(IOException::class)
    override fun close() {
        decryptedData = null
        bytesReadSoFar = 0
        upstream.close()
    }

    override fun addTransferListener(transferListener: TransferListener) {
        upstream.addTransferListener(transferListener)
    }
}
