package com.aqrlei.cachex.lru.encrypt

import com.aqrlei.cachex.lru.ICacheEncrypt


/**
 * created by AqrLei on 2020/4/22
 */
class DefaultCacheEncrypt : ICacheEncrypt {

    private var enableEncrypt: Boolean = false

    fun getEnableEncrypt() = enableEncrypt

    override fun enableEncrypt(enable: Boolean) {
        enableEncrypt = enable
    }

    override fun encrypt(key: String, byteArray: ByteArray?): ByteArray? {
        return byteArray
    }

    override fun decrypt(key: String, byteArray: ByteArray?): ByteArray? {
        return byteArray
    }
}