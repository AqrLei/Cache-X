package com.aqrlei.cachex

import java.io.Closeable

/**
 * created by AqrLei on 3/1/21
 */
interface ICacheJob : Closeable{
    fun start()
}

abstract class CacheJob: ICacheJob {

    abstract fun setBackgroundBlock(block: () -> Unit)
}