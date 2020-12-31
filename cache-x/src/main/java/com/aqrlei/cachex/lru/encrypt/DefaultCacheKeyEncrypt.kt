package com.aqrlei.cachex.lru.encrypt

import com.aqrlei.cachex.lru.ICacheKeyEncrypt


/**
 * created by AqrLei on 2020/4/22
 */
class DefaultCacheKeyEncrypt : ICacheKeyEncrypt {
    override fun encrypt(key: String): String {
        return key
    }
}