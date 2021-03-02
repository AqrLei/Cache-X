package com.aqrlei.cachex.lru.memory

import com.aqrlei.cachex.CacheModel
import com.aqrlei.cachex.Key
import com.aqrlei.cachex.activeResourceJob
import kotlinx.coroutines.CancellationException
import java.lang.ref.ReferenceQueue

/**
 * created by AqrLei on 2/26/21
 * 活动缓存
 */
class ActiveResource private constructor() : CacheModel() {

    companion object {
        fun newInstance() = ActiveResource()
    }

    private val resourceReferenceQueue = ReferenceQueue<ByteResource>()
    private val activeResources = hashMapOf<Key, ByteResourceWeakReference>()
    private var listener: ByteResource.ResourceListener? = null

    init {
        activeResourceJob.setBackgroundBlock {
            cleanReferenceQueue()
        }
        activeResourceJob.start()
    }

    private fun cleanReferenceQueue() {
        while (!isClear) {
            try {
                val ref = resourceReferenceQueue.remove() as? ByteResourceWeakReference
                ref?.let { cleanupActiveReference(it) }
            } catch (e: CancellationException) {

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
        listener?.onResourceReleased(ref.key, newResource)
    }

    fun setListener(listener: ByteResource.ResourceListener) {
        synchronized(listener) {
            synchronized(this) {
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
        isClear = true
        synchronized(bagOfTags) {
            for (value in bagOfTags.values) {
                close(value)
            }
        }
    }
}