package com.aqrlei.cachex

import android.os.Process
import com.aqrlei.cachex.lru.memory.ActiveResource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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

private const val SCOPE_KEY = "com.aqrlei.cachex.CacheModel.SCOPE_KEY"
private const val CACHE_JOB_KEY = "com.aqrlei.cachex.CacheModel.JOB_KEY"

private const val ACTIVE_RESOURCE_JOB_KEY = "com.aqrlei.cachex.lru.memory.ActiveResource.JOB_KEY"
private const val ACTIVE_RESOURCE_SCOPE_KEY =
    "com.aqrlei.cachex.lru.memory.ActiveResource.SCOPE_KEY"

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

val CacheModel.cacheJob: CacheJob
    get() {
        val cacheJob: CacheJob? = this.getTag(CACHE_JOB_KEY)
        if (cacheJob != null) return cacheJob

        return setTagIfAbsent(CACHE_JOB_KEY, CoroutineCacheJob(cacheModelScope))
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
        val cacheJob: CacheJob? = this.getTag(ACTIVE_RESOURCE_JOB_KEY)

        if (cacheJob != null) return cacheJob

        return setTagIfAbsent(ACTIVE_RESOURCE_JOB_KEY, CoroutineCacheJob(activeResourceScope))
    }

internal class CacheClosableScope(context: CoroutineContext) : CoroutineScope, Closeable {

    override val coroutineContext: CoroutineContext = context

    override fun close() {
        coroutineContext.cancel()
    }
}

internal class CoroutineCacheJob(
    private val scope: CoroutineScope) : CacheJob() {
    private val backgroundTaskList = ArrayList<ITaskBackground?>()
    private var coroutineJobList = ArrayList<Job>()
    override fun start() {
        scope.launch {
            try {
                for (backgroundTask in backgroundTaskList) {
                    val job = launch {
                        backgroundTask?.doInBackground()
                    }
                    coroutineJobList.add(job)
                }
            } catch (e: CancellationException) {
                e.printStackTrace()
            }
        }
    }

    override fun close() {
        scope.cancel()
    }

    override fun setBackgroundBlock(vararg iTaskBackground: ITaskBackground) {
        clearPreviousJob()
        backgroundTaskList.addAll(iTaskBackground)
    }

    private fun clearPreviousJob() {
        coroutineJobList.filter { it.isActive }.forEach {
            it.cancel()
        }
        coroutineJobList.clear()
        backgroundTaskList.clear()
    }
}