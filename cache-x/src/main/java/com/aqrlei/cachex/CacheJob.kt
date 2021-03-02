package com.aqrlei.cachex

/**
 * created by AqrLei on 3/1/21
 */
interface ICacheJob {
    fun start()
}

abstract class CacheJob(protected val backgroundBlock: () -> Unit): ICacheJob