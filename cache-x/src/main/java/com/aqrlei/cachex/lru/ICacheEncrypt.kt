package com.aqrlei.cachex.lru

/**
 * created by AqrLei on 2020-01-02
 */
interface ICacheEncrypt {
    fun enableEncrypt(enable: Boolean)
    fun encrypt(key: String, byteArray: ByteArray?): ByteArray?
    fun decrypt(key: String, byteArray: ByteArray?): ByteArray?
}