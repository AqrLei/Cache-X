package com.aqrlei.litecache.lru

/**
 * Created by AqrLei on 2019-06-29
 */
enum class CacheStrategy {
    DEFAULT,    // 磁盘，内存都有缓存
    DISK_ONLY,  //磁盘缓存
    MEMORY_ONLY //内存缓存
}
