package com.aqrlei.litecache.lru

/**
 * created by AqrLei on 2020/4/23
 */
typealias CacheTransformer <T> = (originKey:String,value:ByteArray?)-> T