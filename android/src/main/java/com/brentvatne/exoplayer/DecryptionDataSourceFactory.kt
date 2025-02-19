package com.brentvatne.exoplayer
import androidx.media3.datasource.DataSource

class DecryptionDataSourceFactory(
    private val underlyingFactory: DataSource.Factory,
    private val keyProvider: EncryptionKeyProvider
) : DataSource.Factory {
    override fun createDataSource(): DataSource {
        val underlyingDataSource = underlyingFactory.createDataSource()
        // Get the current encryption keys (this will refresh if needed).
        return DecryptionDataSource(underlyingDataSource, keyProvider)
    }
}
