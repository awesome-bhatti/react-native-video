package com.brentvatne.exoplayer

class DynamicEncryptionKeyProvider : EncryptionKeyProvider {
    private var currentKeys: EncryptionKeys? = null
    private var expiryTime: Long = 0L  // Epoch time in milliseconds

    override fun getEncryptionKeys(): EncryptionKeys {
        // Check if keys are null or if the current time is near the expiry (e.g., less than 1 minute left).
        if (currentKeys == null || System.currentTimeMillis() > expiryTime - 60_000) {
            val newKeys = fetchKeysFromServer()
            currentKeys = newKeys
            // Assume the new keys are valid for 30 minutes.
            expiryTime = System.currentTimeMillis() + (30 * 60 * 1000)
        }
        return currentKeys!!
    }

    private fun fetchKeysFromServer(): EncryptionKeys {
        // Replace this with your actual network call to fetch keys.
        // This example uses dummy values.
        val key = "1234567890123456".toByteArray(Charsets.UTF_8)  // For AES-128, 16 bytes.
        val iv = "abcdefghijklmnop".toByteArray(Charsets.UTF_8)      // 16 bytes IV.
        return EncryptionKeys(key, iv)
    }
}

