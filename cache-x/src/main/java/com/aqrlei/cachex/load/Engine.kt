package com.aqrlei.cachex.load

import com.aqrlei.cachex.CacheModel
import com.aqrlei.cachex.ITaskBackground
import com.aqrlei.cachex.Key
import com.aqrlei.cachex.cacheJob
import com.aqrlei.cachex.lru.disk.DiskCache
import com.aqrlei.cachex.lru.memory.ActiveResource
import com.aqrlei.cachex.lru.memory.ByteResource
import com.aqrlei.cachex.lru.memory.cache.LruResourceCache
import com.aqrlei.cachex.mainHandler

/**
 * created by AqrLei on 2/27/21
 */
class Engine(
    private val activeResource: ActiveResource,
    private val cache: LruResourceCache,
    private val diskCache: DiskCache
) : CacheModel(), ByteResource.ResourceListener, ICacheEngine {

    init {
        activeResource.setListener(this)
    }
    override fun onResourceReleased(key: Key, resource: ByteResource) {
        activeResource.deactivate(key)
        if (resource.isMemoryCacheable) {
            cache.put(key, resource)
        }
    }

    override fun load(
        key: Key,
        callback: ResourceCallback,
        isMemoryCacheable: Boolean
    ): Any? {
        val memoryResource: ByteResource?
        synchronized(this) {
            memoryResource = loadFromMemory(key)
            if (memoryResource == null) {
                return loadFromDisk(key, callback, isMemoryCacheable)
            }
        }
        callback.onResourceReady(memoryResource!!, DataResource.MEMORY_CACHE)
        return null
    }

    override fun put(key: Key, byteArray: ByteArray, isMemoryCacheable: Boolean) {
        val resource = ByteResource(key, byteArray, isMemoryCacheable)
        diskCache.put(key, resource)
        activeResource.activate(key, resource)
    }

    private fun loadFromDisk(
        key: Key,
        callback: ResourceCallback,
        isMemoryCacheable: Boolean
    ): Any? {
        cacheJob.setBackgroundBlock(ITaskBackground {
            val diskCacheValue = diskCache.get(key)
            if (diskCacheValue == null) {
                mainHandler.post {
                    callback.onLoadFailed(CacheLoadFailureException("no cache"))
                }
            } else {
                val diskResource = ByteResource(key, diskCacheValue, isMemoryCacheable)
                activeResource.activate(key, diskResource)
                mainHandler.post {
                    callback.onResourceReady(diskResource, DataResource.DISK_CACHE)
                }
            }
        })
        cacheJob.start()
        return null
    }

    private fun loadFromMemory(key: Key): ByteResource? {

        val active = loadFromActiveResource(key)
        if (active != null) return active

        val cached = loadFromCache(key)
        if (cached != null) return cached

        return null
    }

    private fun loadFromActiveResource(key: Key) = activeResource.get(key)

    private fun loadFromCache(key: Key): ByteResource? {
        val cached = cache.remove(key)
        if (cached != null) {
            activeResource.activate(key, cached)
        }
        return cached
    }
}