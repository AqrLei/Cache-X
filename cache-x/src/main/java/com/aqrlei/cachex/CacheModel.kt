package com.aqrlei.cachex

import java.io.Closeable
import java.io.IOException

/**
 * created by AqrLei on 3/1/21
 */
abstract class CacheModel  {

    protected val bagOfTags = hashMapOf<String, Any>()


    @Volatile
    protected var isClear: Boolean = false

    protected fun close(any: Any) {
        if (any is Closeable) {
            try {
                any.close()
            } catch (e: IOException) { //TODO
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T: Any?> getTag(key: String): T {
        synchronized(bagOfTags) {
            return bagOfTags[key] as T
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> setTagIfAbsent(key: String, newValue: T): T {
        val previous = bagOfTags[key]
        if (previous == null) {
            bagOfTags[key] = newValue
        }
        val result = previous ?: newValue
        if (isClear){
            close(result)
        }
        return result as T
    }

}