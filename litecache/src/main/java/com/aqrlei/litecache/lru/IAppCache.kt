package com.aqrlei.litecache.lru

import com.aqrlei.litecache.lru.CacheTransformer
import com.aqrlei.litecache.lru.ICacheTask

/**
 * Created by AqrLei on 2019-06-29
 */
interface IAppCache {

    fun <T> load(
        originKey: String,
        callback: (T) -> Unit,
        transformer: CacheTransformer<T>,
        loadCacheTask: ICacheTask,
        loadFromCustom: () -> ByteArray?)

    fun put(originKey: String, value: ByteArray, loadCacheTask: ICacheTask)

    fun <T> read(
        originKey: String,
        callback: (T) -> Unit,
        transformer: CacheTransformer<T>,
        loadCacheTask: ICacheTask
    )

    fun <T> readFromMemory(originKey: String, transformer: CacheTransformer<T>): T

    fun <T> readFromDisk(originKey: String,
                         callback: (T) -> Unit,
                         transformer: CacheTransformer<T>,
                         loadCacheTask: ICacheTask
    )

    fun clear(): Boolean

    fun clearDisk(): Boolean
    fun clearMemory()

    fun getCacheName(): String

}