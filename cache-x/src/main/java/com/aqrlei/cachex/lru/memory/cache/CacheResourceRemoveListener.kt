package com.aqrlei.cachex.lru.memory.cache

import com.aqrlei.cachex.Key
import com.aqrlei.cachex.lru.memory.ByteResource

/**
 * created by AqrLei on 2/27/21
 */
interface CacheResourceRemoveListener {
    fun onResourceRemoved(evicted: Boolean , key: Key, oldValue: ByteResource?, newValue: ByteResource?)
}