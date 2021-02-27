package com.aqrlei.cachex.lru.memory.cache

import android.util.LruCache
import com.aqrlei.cachex.Key
import com.aqrlei.cachex.lru.memory.ByteResource

/**
 * created by AqrLei on 2/27/21
 */
class LruResourceCache(
    maxSize: Int,
    private val listener: CacheResourceRemoveListener? = null
) : LruCache<Key, ByteResource>(maxSize), MemoryCache {
    override fun entryRemoved(
        evicted: Boolean,
        key: Key,
        oldValue: ByteResource?,
        newValue: ByteResource?
    ) {
        listener?.onResourceRemoved(evicted, key, oldValue, newValue)
    }

    override fun sizeOf(key: Key, value: ByteResource?): Int {
        return value?.getSize() ?: super.sizeOf(key, value)
    }

    override fun clearMemory() {
        evictAll()
    }

    override fun trimMemory(level: Int) {
        if (level >= android.content.ComponentCallbacks2.TRIM_MEMORY_BACKGROUND) {
            clearMemory()
        } else if (level >= android.content.ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN
            || level == android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL) {
            trimToSize(maxSize() / 2)
        }
    }
}