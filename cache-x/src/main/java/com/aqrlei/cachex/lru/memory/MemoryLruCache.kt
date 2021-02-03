package com.aqrlei.cachex.lru.memory

import android.util.LruCache

/**
 * created by AqrLei on 2/3/21
 */
class MemoryLruCache<V>(maxSize: Int) : LruCache<String, V>(maxSize) {
    override fun sizeOf(key: String?, value: V): Int {
        return super.sizeOf(key, value)
    }

    override fun entryRemoved(evicted: Boolean, key: String?, oldValue: V, newValue: V) {
        super.entryRemoved(evicted, key, oldValue, newValue)

    }
}