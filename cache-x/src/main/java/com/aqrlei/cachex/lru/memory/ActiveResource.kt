package com.aqrlei.cachex.lru.memory

import android.os.Process
import com.aqrlei.cachex.Key
import java.lang.ref.ReferenceQueue
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import kotlin.concurrent.thread

/**
 * created by AqrLei on 2/26/21
 * 活动缓存
 */
class ActiveResource private constructor(private val monitorClearedResourcesExecutor: Executor) {

    companion object {
        fun newInstance(): ActiveResource {
            return ActiveResource(
                Executors.newSingleThreadExecutor(
                ThreadFactory { r ->
                    thread(
                        start = false,
                        name = "cache-active-resources",
                        priority = Process.THREAD_PRIORITY_BACKGROUND
                    ) {
                        r.run()
                    }
                }
            ))
        }
    }

    private val resourceReferenceQueue = ReferenceQueue<ByteResource>()
    private val activeResources = hashMapOf<Key, ByteResourceWeakReference>()
    private var listener: ByteResource.ResourceListener? = null

    @Volatile
    private var isShutdown: Boolean = false

    init {
        monitorClearedResourcesExecutor.execute {
            cleanReferenceQueue()
        }
    }

    private fun cleanReferenceQueue() {
        while (!isShutdown) {
            try {
                val ref = resourceReferenceQueue.remove() as? ByteResourceWeakReference
                ref?.let { cleanupActiveReference(it) }

            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
    }

    private fun cleanupActiveReference(ref: ByteResourceWeakReference) {
        synchronized(this) {
            activeResources.remove(ref.key)
            if (!ref.isCacheable || ref.resource == null) {
                return
            }
        }
        val newResource = ByteResource(ref.key, ref.resource!!, true)
        //TODO 存到LruCache缓存里去
        listener?.onResourceReleased(ref.key, newResource)
    }

    fun setListener(listener: ByteResource.ResourceListener) {
        synchronized(listener) {
            synchronized(this){
                this.listener = listener
            }
        }
    }

    /**
     * 从磁盘缓存中取/从外部获取后加入到活动缓存
     */
    @Synchronized
    fun activate(key: Key, resource: ByteResource) {
        val toPut = ByteResourceWeakReference(key, resource, resourceReferenceQueue)
        val removed = activeResources.put(key, toPut)
        removed?.reset()
    }

    /**
     * 被回收
     */
    @Synchronized
    fun deactivate(key: Key) {
        val removed = activeResources.remove(key)
        removed?.reset()
    }

    @Synchronized
    fun get(key: Key): ByteResource? {
        val activeRef = activeResources[key] ?: return null

        val active = activeRef.get()
        if (active == null) {
            cleanupActiveReference(activeRef)
        }
        return active
    }

    fun shutdown() {
        isShutdown = true
       if (monitorClearedResourcesExecutor is ExecutorService){
           com.aqrlei.cachex.util.Executors.shutdownAndAwaitTermination(monitorClearedResourcesExecutor)
       }
    }
}