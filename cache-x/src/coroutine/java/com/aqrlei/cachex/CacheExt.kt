package com.aqrlei.cachex

import android.os.Process
import kotlinx.coroutines.*
import java.io.Closeable
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import kotlin.concurrent.thread
import kotlin.coroutines.CoroutineContext

/**
 * created by AqrLei on 3/1/21
 */

private const val JOB_KEY = "com.aqrlei.cachex.CacheModel.JOB_KEY"

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
        val scope: CoroutineScope? = this.getTag(JOB_KEY)
        if (scope != null) return scope

        return setTagIfAbsent(JOB_KEY, CacheClosableScope(SupervisorJob() + Dispatchers.IO))
    }

internal class CacheClosableScope(context: CoroutineContext) : CoroutineScope, Closeable {

    override val coroutineContext: CoroutineContext = context

    override fun close() {
        coroutineContext.cancel()
    }
}

class CoroutineCacheJob(private val scope: CoroutineScope,
                        block: ()-> Unit): CacheJob(block) {
    override fun start() {
        scope.launch {
            backgroundBlock.invoke()
        }
    }
}


