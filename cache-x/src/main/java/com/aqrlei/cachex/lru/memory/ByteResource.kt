package com.aqrlei.cachex.lru.memory

import com.aqrlei.cachex.Key

/**
 * created by AqrLei on 2/26/21
 */
class ByteResource(
    val key: Key,
    private val byteArray: ByteArray,
    val isMemoryCacheable: Boolean) {

    fun getSize(): Int = byteArray.size

    fun getResource() = byteArray

    fun interface  ResourceListener {
        fun onResourceReleased(key:Key, resource: ByteResource)
    }
}