package com.aqrlei.cachex

import android.os.Process
import com.aqrlei.cachex.lru.memory.ActiveResource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.Closeable
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import kotlin.concurrent.thread
import kotlin.coroutines.CoroutineContext

/**
 * created by AqrLei on 3/1/21
 */

private const val SCOPE_KEY = "com.aqrlei.cachex.CacheModel.JOB_KEY"
private const val CACHE_JOB_KEY = "com.aqrlei.cachex.CacheModel.CACHE_JOB_KEY"
private const val ACTIVE_RESOURCE_SCOPE_KEY = "com.aqrlei.cachex.lru.memory.ActiveResource.JOB_KEY"

private val activeResourceDispatcher = Executors.newSingleThreadExecutor(ThreadFactory { r ->
    thread(
        start = false,
        name = "cache-active_resources",
        priority = Process.THREAD_PRIORITY_BACKGROUND) { r.run() }
}).asCoroutineDispatcher()

/**
 * default
 */
val CacheModel.cacheModelScope: CoroutineScope
    get() {
        val scope: CoroutineScope? = this.getTag(SCOPE_KEY)
        if (scope != null) return scope

        return setTagIfAbsent(SCOPE_KEY, CacheClosableScope(SupervisorJob() + Dispatchers.IO))
    }

/**
 * for ActiveResource
 */
val ActiveResource.activeResourceScope: CoroutineScope
    get() {
        val scope: CoroutineScope? = this.getTag(ACTIVE_RESOURCE_SCOPE_KEY)
        if (scope != null) return scope
        return setTagIfAbsent(
            ACTIVE_RESOURCE_SCOPE_KEY,
            CacheClosableScope(SupervisorJob() + activeResourceDispatcher))
    }

val ActiveResource.activeResourceJob: CacheJob
    get() {
        val cacheJob: CacheJob? = this.getTag(CACHE_JOB_KEY)

        if (cacheJob != null) return cacheJob

        return setTagIfAbsent(CACHE_JOB_KEY, CoroutineCacheJob(activeResourceScope))
    }

internal class CacheClosableScope(context: CoroutineContext) : CoroutineScope, Closeable {

    override val coroutineContext: CoroutineContext = context

    override fun close() {
        coroutineContext.cancel()
    }
}

internal class CoroutineCacheJob(
    private val scope: CoroutineScope) : CacheJob() {
    private var backgroundBlock: (() -> Unit)? = null
    override fun start() {
        scope.launch {
            try {
                backgroundBlock?.invoke()
            } catch (e: CancellationException) {
                e.printStackTrace() // todo
            }
        }
    }

    override fun close() {
        scope.cancel()
    }

    override fun setBackgroundBlock(block: () -> Unit) {
        backgroundBlock?.invoke()
    }
}