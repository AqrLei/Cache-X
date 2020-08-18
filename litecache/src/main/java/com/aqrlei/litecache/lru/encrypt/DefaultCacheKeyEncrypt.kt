package com.aqrlei.litecache.lru.encrypt

import com.aqrlei.litecache.lru.ICacheKeyEncrypt


/**
 * created by AqrLei on 2020/4/22
 */
class DefaultCacheKeyEncrypt : ICacheKeyEncrypt {
    override fun encrypt(key: String): String {
        return key
    }
}