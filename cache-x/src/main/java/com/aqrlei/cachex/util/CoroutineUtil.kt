package com.aqrlei.cachex.util

import android.os.Process
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import java.io.Closeable
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import kotlin.concurrent.thread
import kotlin.coroutines.CoroutineContext

/**
 * created by AqrLei on 2/27/21
 */
object CoroutineUtil {


    fun getActiveResourceCoroutineContext(name: String) =
        Executors.newSingleThreadExecutor(ThreadFactory { r ->
            thread(
                start = false,
                name = name,
                priority = Process.THREAD_PRIORITY_BACKGROUND
            ) { r.run() }
        }).asCoroutineDispatcher() + CoroutineExceptionHandler { _, _ ->
            //TODO do nothing
        }

}


internal class CacheClosableScope(context : CoroutineContext): CoroutineScope, Closeable {

    override val coroutineContext: CoroutineContext = context

    override fun close() {
        coroutineContext.cancel()
    }
}