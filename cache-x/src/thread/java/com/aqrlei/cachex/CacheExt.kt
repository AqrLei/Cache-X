package com.aqrlei.cachex

import com.aqrlei.cachex.lru.memory.ActiveResource
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/**
 * created by AqrLei on 3/2/21
 */
private const val CACHE_JOB_KEY = "com.aqrlei.cachex.CacheModel.CACHE_JOB_KEY"

val ActiveResource.activeResourceJob: CacheJob
    get() {
        val cacheJob: CacheJob? = this.getTag(CACHE_JOB_KEY)
        if (cacheJob != null) return cacheJob

        return setTagIfAbsent(CACHE_JOB_KEY, ThreadCacheJob(
            Executors.newSingleThreadExecutor(
                ThreadFactory { r ->
                    thread(
                        start = false,
                        name = "cache-active-resources",
                        priority = android.os.Process.THREAD_PRIORITY_BACKGROUND) {
                        r.run()
                    }
                }
            )))
    }

internal class ThreadCacheJob(
    private val activeResourceExecutor: Executor
) : CacheJob() {
    private var backgroundBlock: (() -> Unit)? = null

    override fun start() {
        activeResourceExecutor.execute {
            backgroundBlock?.invoke()
        }
    }

    override fun setBackgroundBlock(block: () -> Unit) {
        backgroundBlock = block
    }

    override fun close() {
        (activeResourceExecutor as? ExecutorService)?.let { pool ->
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
}