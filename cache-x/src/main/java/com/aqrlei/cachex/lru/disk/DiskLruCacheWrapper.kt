package com.aqrlei.cachex.lru.disk

import com.aqrlei.cachex.Key
import com.aqrlei.cachex.lru.memory.ByteResource
import java.io.BufferedOutputStream
import java.io.File
import java.io.IOException

/**
 * created by AqrLei on 3/2/21
 */
//TODO
class DiskLruCacheWrapper(
    private val directory: File,
    private val maxSize: Long) : DiskCache{
    companion object {
        private const val APP_VERSION = 1
        private const val VALUE_COUNT = 1

        fun create(directory: File, maxSize: Long) = DiskLruCacheWrapper(directory, maxSize)
    }

    private var diskLruCache: DiskLruCache? = null
    private val writeLocker = DiskCacheWriteLocker()

    @Throws(IOException::class)
    @Synchronized
    private fun getDiskCache(): DiskLruCache {
        if (diskLruCache == null) {
            diskLruCache = DiskLruCache.open(directory, APP_VERSION, VALUE_COUNT, maxSize)
        }
        return diskLruCache!!
    }

    override fun get(key: Key): ByteArray? {
        val safeKey = key.toString()
        var result: ByteArray? = null
        try {
            val value = getDiskCache().get(safeKey)
            result = value?.getInputStream(0)?.readBytes()
        } catch (e: IOException) {
            // log
        }
        return result
    }

    override fun put(key: Key, resource: ByteResource) {
        // safeKey
        val safeKey = key.toString()

        writeLocker.acquire(safeKey)
        try {
            try {
                val diskCache = getDiskCache()
                val current = diskCache.get(safeKey)
                if (current != null) return

                val editor = diskCache.edit(safeKey)
                    ?: throw IllegalStateException("Had two simultaneous puts for: $safeKey")
                editor.newOutputStream(0).run {
                    val bufferOut = BufferedOutputStream(this)
                    try {
                        bufferOut.write(resource.getResource())
                        editor.commit()
                    } finally {
                        editor.abortUnlessCommitted()
                        bufferOut.close()
                    }
                }
            } catch (e: IOException) {
                //LOG
            }
        } finally {
            writeLocker.release(safeKey)
        }
    }

    override fun delete(key: Key) {
        // safeKey
        val safeKey = key.toString()
        try {
            getDiskCache().remove(safeKey)
        } catch (e: IOException) {
            // log
        }
    }

    @Synchronized
    override fun clear() {
        try {
            getDiskCache().delete()
        } catch (e: IOException) {
            //log
        } finally {
            resetDiskCache()
        }
    }

    @Synchronized
    private fun resetDiskCache() {
        diskLruCache = null
    }
}