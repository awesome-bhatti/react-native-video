package com.brentvatne.exoplayer

data class EncryptionKeys(val key: ByteArray, val iv: ByteArray)

interface EncryptionKeyProvider {
    /**
     * Returns the current encryption keys. The implementation can check if the keys are about to expire
     * and fetch new ones from your backend if necessary.
     */
    fun getEncryptionKeys(): EncryptionKeys
}

