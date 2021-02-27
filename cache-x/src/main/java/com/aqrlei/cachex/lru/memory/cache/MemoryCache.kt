package com.aqrlei.cachex.lru.memory.cache

/**
 * created by AqrLei on 2/27/21
 */
interface MemoryCache {


    /**
     *  Evict all items from the memory cache.
     **/
    fun clearMemory()

    /**
     * @param level [android.content.ComponentCallbacks2]
     */
    fun trimMemory(level: Int)
}