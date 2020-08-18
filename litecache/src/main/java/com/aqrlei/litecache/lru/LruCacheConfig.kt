package com.aqrlei.litecache.lru

import com.aqrlei.litecache.lru.CacheStrategy
import com.aqrlei.litecache.lru.ICacheEncrypt
import com.aqrlei.litecache.lru.encrypt.DefaultCacheEncrypt
import com.aqrlei.litecache.lru.encrypt.DefaultCacheKeyEncrypt

/**
 * created by AqrLei on 2020/4/22
 */
class LruCacheConfig {
    companion object {
        // 10M
        val DEFAULT_DISK_CACHE_SIZE = 10 * 1024 * 1024L

        // 占应用分配内存的 1/20
        val DEFAULT_MEMORY_CACHE_RATIO = 1.00F / 20.00F
        val DEFAULT_CACHE_STRATEGY = CacheStrategy.DEFAULT
        const val DEFAULT_CACHE_DISK_NAME = "cache"
    }

    var diskCacheSize: Long = DEFAULT_DISK_CACHE_SIZE
    var memoryCacheRatio: Float = DEFAULT_MEMORY_CACHE_RATIO
    var cacheStrategy: CacheStrategy = DEFAULT_CACHE_STRATEGY
    var cacheEncrypt: ICacheEncrypt = DefaultCacheEncrypt()
    var cacheKeyEncrypt: ICacheKeyEncrypt = DefaultCacheKeyEncrypt()
    var diskCacheFileName: String = DEFAULT_CACHE_DISK_NAME
}