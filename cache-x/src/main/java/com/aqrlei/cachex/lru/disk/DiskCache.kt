package com.aqrlei.cachex.lru.disk

import com.aqrlei.cachex.Key
import com.aqrlei.cachex.lru.memory.ByteResource

/**
 * created by AqrLei on 3/3/21
 */
interface DiskCache {

    fun get(key: Key): ByteArray?

    fun put(key: Key, resource: ByteResource)

    fun delete(key: Key)

    fun clear()
}