package com.aqrlei.cachex.lru

import android.content.Context

/**
 * created by AqrLei on 2020-01-02
 */
object LruCacheHelper {

    private val cacheMap = mutableMapOf<String, IAppCache>()

    @JvmStatic
    fun getDefaultCache(
        context: Context,
        lruCacheConfig: (LruCacheConfig.() -> Unit)? = null): LruAppCache {
        return LruAppCache.Builder(context)
            .cacheConfig(lruCacheConfig)
            .build().also {
                cacheMap[it.getCacheName()] = it
            }
    }


    fun clearCache(cacheName: String) {
        cacheMap[cacheName]?.clear()
    }

    fun clearMemory(cacheName: String) {
        cacheMap[cacheName]?.clearMemory()
    }

    fun clearDisk(cacheName: String) {
        cacheMap[cacheName]?.clearDisk()
    }
}