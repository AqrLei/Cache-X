package com.aqrlei.cachex

import com.aqrlei.cachex.lru.memory.ByteResource

/**
 * created by AqrLei on 3/2/21
 */
interface ResourceCallback {

    fun onResourceReady(resource: ByteResource, dataResource: DataResource)

    fun onLoadFailed(t: Throwable)
}