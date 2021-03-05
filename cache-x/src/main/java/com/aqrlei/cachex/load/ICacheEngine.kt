package com.aqrlei.cachex.load

import com.aqrlei.cachex.Key

/**
 * created by AqrLei on 3/5/21
 */
interface ICacheEngine {

    fun load(key: Key, callback: ResourceCallback, isMemoryCacheable: Boolean) : Any?

    fun put(key: Key, byteArray: ByteArray, isMemoryCacheable: Boolean)
}