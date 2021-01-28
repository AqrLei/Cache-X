package com.aqrlei.cachex.lru

import android.content.Context
import android.util.LruCache
import com.aqrlei.cachex.lru.disklrucache.DiskLruCache
import java.io.BufferedOutputStream
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created by AqrLei on 2019-06-28
 */
class LruAppCache private constructor(
    private val context: WeakReference<Context>,
    private val lruCacheConfig: LruCacheConfig
) : AbstractCache(lruCacheConfig.cacheEncrypt) {

    companion object {
        private var memoryCache: LruCache<String, ByteArray>? = null
    }

    private var cacheDir: File? = null

    private val keySet = TreeSet<String>()
    private var diskLruCache: DiskLruCache?

    init {
        with(lruCacheConfig) {
            memoryCache = object :
                LruCache<String, ByteArray>((runtimeMaxMemory() * memoryCacheRatio).toInt()) {
                override fun sizeOf(key: String?, value: ByteArray?): Int {
                    return value?.size ?: 1
                }
            }
            diskLruCache = context.get()?.run {
                getDiskLruCache(this)
            }
        }
    }

    override fun getCacheName(): String = lruCacheConfig.diskCacheFileName

    override fun put(originKey: String, value: ByteArray, loadCacheTask: ICacheTask) {
        val key = generateKey(originKey)
        saveToCache(key, value, loadCacheTask)
    }

    override fun <T> read(
        originKey: String,
        callback: (T) -> Unit,
        transformer: CacheTransformer<T>,
        loadCacheTask: ICacheTask) {
        val key = generateKey(originKey)
        loadFromCache(key, loadCacheTask, "cache-read-$key") {
            callback(transformer(originKey, it))
        }
    }

    override fun <T> readFromMemory(originKey: String, transformer: CacheTransformer<T>): T {
        val key = generateKey(originKey)
        return transformer(originKey, loadFromMemory(key))
    }

    override fun <T> readFromDisk(
        originKey: String,
        callback: (T) -> Unit,
        transformer: CacheTransformer<T>,
        loadCacheTask: ICacheTask) {
        val key = generateKey(originKey)
        loadFromDisk(key, "cache-readFromDisk-${key}", loadCacheTask) {
            callback(transformer(originKey, it))
        }
    }

    override fun <T> load(
        originKey: String,
        callback: (T) -> Unit,
        transformer: CacheTransformer<T>,
        loadCacheTask: ICacheTask,
        loadFromCustom: () -> ByteArray?) {
        val key = generateKey(originKey)
        loadFromCache(key, loadCacheTask, "cache-load-${key}") { result ->
            if (result == null) {
                val resultByteArray = loadFromCustom()
                saveToCache(key, resultByteArray, loadCacheTask)
                callback(transformer(originKey, resultByteArray))
            } else {
                callback(transformer(originKey, result))
            }
        }
    }

    private fun loadFromCache(
        key: String,
        loadCacheTask: ICacheTask,
        taskName: String,
        callback: (ByteArray?) -> Unit) {
        when (lruCacheConfig.cacheStrategy) {
            CacheStrategy.DEFAULT -> {
                if (keySet.contains(key)) {
                    loadFromMemory(key)?.let(callback)
                        ?: loadFromDisk(key, taskName, loadCacheTask, callback)
                } else {
                    loadFromDisk(key, taskName, loadCacheTask, callback)
                }
            }
            CacheStrategy.DISK_ONLY -> {
                loadFromDisk(key, taskName, loadCacheTask, callback)
            }
            CacheStrategy.MEMORY_ONLY -> {
                callback(loadFromMemory(key))
            }
        }
    }

    private fun loadFromMemory(key: String): ByteArray? {
        return decrypt(key, memoryCache?.get(key))
    }

    private fun loadFromDisk(
        key: String,
        taskName: String,
        cacheTask: ICacheTask,
        callback: (ByteArray?) -> Unit) {
        cacheTask.doInBackground(taskName) {
            val result = diskLruCache?.get(key)?.getInputStream(0)?.readBytes()?.apply {
                if (lruCacheConfig.cacheStrategy != CacheStrategy.DISK_ONLY) {
                    saveToMemory(key, this)
                }
            }
            callback(decrypt(key, result))
        }
    }

    private fun saveToCache(key: String, byteArray: ByteArray?, loadCacheTask: ICacheTask) {
        encrypt(key, byteArray)?.let { enByteArray ->
            when (lruCacheConfig.cacheStrategy) {
                CacheStrategy.DEFAULT -> {
                    loadCacheTask.doInBackground(taskName = "cache-save-$key") {
                        saveToDisk(key, enByteArray)
                    }
                    saveToMemory(key, enByteArray)
                }
                CacheStrategy.MEMORY_ONLY -> {
                    saveToMemory(key, enByteArray)
                }
                CacheStrategy.DISK_ONLY -> {
                    loadCacheTask.doInBackground(taskName = "cache-save-$key") {
                        saveToDisk(key, enByteArray)
                    }
                }
            }
        }
    }

    private fun saveToMemory(key: String, byteArray: ByteArray) {
        memoryCache?.put(key, byteArray)
    }

    private fun saveToDisk(key: String, byteArray: ByteArray) {
        diskLruCache?.let {
            try {
                it.edit(key)?.let { editor ->
                    val bufferOut = BufferedOutputStream(editor.newOutputStream(0))
                    try {
                        bufferOut.write(byteArray)
                        editor.commit()
                    } catch (e: IOException) {
                        editor.abort()
                    } finally {
                        bufferOut.close()
                        diskCacheFlush()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun diskCacheFlush() {
        try {
            diskLruCache?.flush()
        } catch (e: IOException) {
        }
    }

    private fun generateKey(originKey: String): String {
        val key = lruCacheConfig.cacheKeyEncrypt.encrypt(originKey)
        keySet.add(key)
        return key
    }

    private fun getDiskLruCache(context: Context): DiskLruCache {
        cacheDir = getAppCacheDirFile(context, lruCacheConfig.diskCacheFileName)
        return DiskLruCache.open(
            cacheDir!!, getAppVersion(context), 1,
            lruCacheConfig.diskCacheSize
        )
    }

    override fun clear(): Boolean {
        clearMemory()
        return clearDisk()
    }

    override fun clearDisk(): Boolean {
        return try {
            var result = false
            if (cacheDir?.isDirectory == true) {
                cacheDir?.listFiles()?.forEach {
                    result = it.delete()
                }
            }
            result
        } catch (e: SecurityException) {
            false
        }
    }

    override fun clearMemory() {
        memoryCache?.evictAll()
    }

    class Builder(private val context: Context) {
        private var lruCacheConfig: LruCacheConfig? = null

        fun cacheConfig(block: (LruCacheConfig.() -> Unit)?): Builder {
            block ?: return this
            lruCacheConfig = LruCacheConfig().apply(block)
            return this
        }

        fun build(): LruAppCache {
            lruCacheConfig ?: throw NullPointerException("")
            return LruAppCache(
                WeakReference(context), lruCacheConfig!!)
        }
    }
}