package com.aqrlei.cachex.lru.disk

import java.io.File

/**
 * created by AqrLei on 3/2/21
 */
class DiskLruCacheWrapper(
    private val directory: File,
    private val maxSize: Long) {
    companion object {
        private const val APP_VERSION = 1
        private const val VALUE_COUNT = 1
    }



}