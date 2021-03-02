package com.aqrlei.cachex.lru.disk

import java.io.File
import java.io.IOException
import java.io.Writer
import java.security.Key

/**
 * created by AqrLei on 3/2/21
 */
//TODO
class DiskLruCacheWrapper(
    private val directory: File,
    private val maxSize: Long) {
    companion object {
        private const val APP_VERSION = 1
        private const val VALUE_COUNT = 1
    }

    private var diskLruCache: DiskLruCache? = null

    @Throws(IOException::class)
    @Synchronized
    private fun getDiskCache(): DiskLruCache {
        if (diskLruCache == null) {
            diskLruCache = DiskLruCache.open(directory, APP_VERSION, VALUE_COUNT, maxSize)
        }
        return diskLruCache!!
    }


    fun put(key: Key, writer: Writer){
        // safeKey
        val safeKey = key.toString()

        // writeLocker.acquire(safeKey)
        try {
            try {
                val diskCache = getDiskCache()
                val current = diskCache.get(safeKey)
                if (current != null) return

                val editor = diskCache.edit(safeKey)
                    ?: throw IllegalStateException("Had two simultaneous puts for: $safeKey")

                try {
                    val file = editor
                }finally {
                    editor.commit()
                }
            }catch (e: IOException) {
                //LOG
            }

        }finally {
            //writeLocker.release(safeKey)
        }


    }


    fun delete(key: Key){
        // safeKey
        val safeKey = key.toString()
        try {
           getDiskCache().remove(safeKey)
        }catch (e: IOException){
            // log
        }
    }

    @Synchronized
    fun clear() {
        try {
            getDiskCache().delete()
        }catch (e: IOException){
            //log
        }finally {
            resetDiskCache()
        }
    }

    @Synchronized
    private fun resetDiskCache(){
        diskLruCache = null
    }

}