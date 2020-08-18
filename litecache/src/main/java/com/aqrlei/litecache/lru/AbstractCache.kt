package com.aqrlei.litecache.lru

import android.content.Context
import android.content.pm.PackageManager
import java.io.File

/**
 * Created by AqrLei on 2019-07-01
 */
abstract class AbstractCache(protected val cacheEncrypt: ICacheEncrypt) : IAppCache {

    fun encrypt(key: String, byteArray: ByteArray?): ByteArray? {
        return cacheEncrypt.encrypt(key, byteArray)
    }

    fun decrypt(key: String, byteArray: ByteArray?): ByteArray? {
        return cacheEncrypt.decrypt(key, byteArray)
    }

    protected fun getAppVersion(context: Context): Int {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            1
        }
    }

    protected fun runtimeMaxMemory() = Runtime.getRuntime().maxMemory()

    protected fun getAppCacheDirFile(context: Context, uniqueName: String): File {
        val cachePath = context.externalCacheDir?.path ?: context.cacheDir.path

        return File("$cachePath${File.separator}$uniqueName").apply {
            if (!exists()){
                mkdirs()
            }
        }
    }
}