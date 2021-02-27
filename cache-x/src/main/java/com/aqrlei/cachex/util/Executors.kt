package com.aqrlei.cachex.util

import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit

/**
 * created by AqrLei on 2/27/21
 */
object Executors {
    fun shutdownAndAwaitTermination(pool: ExecutorService) {
        val shutdownSeconds: Long = 5
        pool.shutdownNow()
        try {
            if (!pool.awaitTermination(shutdownSeconds, TimeUnit.SECONDS)) {
                pool.shutdownNow()
                if (!pool.awaitTermination(shutdownSeconds, TimeUnit.SECONDS)) {
                    throw RuntimeException("Failed to shutdown")
                }
            }
        } catch (ie: InterruptedException) {
            pool.shutdownNow()
            Thread.currentThread().interrupt()
            throw RuntimeException(ie)
        }
    }
}