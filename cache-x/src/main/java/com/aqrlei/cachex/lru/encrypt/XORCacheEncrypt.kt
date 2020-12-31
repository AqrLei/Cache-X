package com.aqrlei.cachex.lru.encrypt

import com.aqrlei.cachex.lru.ICacheEncrypt
import kotlin.experimental.xor

/**
 * created by AqrLei on 2020/4/22
 */
class XORCacheEncrypt : ICacheEncrypt {

    private var enableEncrypt: Boolean = false

    fun getEnableEncrypt() = enableEncrypt

    override fun enableEncrypt(enable: Boolean) {
        enableEncrypt = enable
    }

    override fun encrypt(key: String, byteArray: ByteArray?): ByteArray? {
        return code(key, byteArray)
    }

    override fun decrypt(key: String, byteArray: ByteArray?): ByteArray? {
        return code(key, byteArray)
    }

    private fun code(key: String, byteArray: ByteArray?): ByteArray? {
        return byteArray?.apply {
            val keyByteArray = key.toByteArray()
            for (i in this.indices) {
                this[i] = this[i] xor keyByteArray[i % keyByteArray.size]
            }
        }
    }
}