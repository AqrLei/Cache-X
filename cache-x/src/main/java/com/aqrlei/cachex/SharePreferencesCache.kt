package com.aqrlei.cachex

import android.content.Context
import android.content.SharedPreferences
import java.lang.ref.WeakReference

/**
 * Created by AqrLei on 2019-05-27
 */
@Suppress("unused")
class SharePreferencesCache private constructor() {

    private lateinit var editor: SharedPreferences.Editor
    private lateinit var shared: SharedPreferences

    companion object {
        const val LOG_TAG = "SharePreferencesCache"
        fun get(): SharePreferencesCache {
            if (reference == null) throw NullPointerException("reference must not be null")
            return Holder.cache
        }

        private var reference: WeakReference<Context>? = null
        private var name: String = ""
        fun init(context: Context, name: String = "") {
            reference = WeakReference(context)
            Companion.name = name
        }
    }

    private object Holder {
        val cache = SharePreferencesCache()
    }

    init {
        reference?.get()?.let {
            shared = it.getSharedPreferences(name, Context.MODE_PRIVATE)
            editor = shared.edit().apply { apply() }
        }
    }

    fun putString(key: String, value: String): SharePreferencesCache {
        editor.putString(key, value)
        return this
    }

    fun putFloat(key: String, value: Float): SharePreferencesCache {
        editor.putFloat(key, value)
        return this
    }

    fun putLong(key: String, value: Long): SharePreferencesCache {
        editor.putLong(key, value)
        return this
    }

    fun putInt(key: String, value: Int): SharePreferencesCache {
        editor.putInt(key, value)
        return this
    }

    fun putBoolean(key: String, value: Boolean): SharePreferencesCache {
        editor.putBoolean(key, value)
        return this
    }

    fun commit() {
        editor.commit()
    }

    fun apply(){
        editor.apply()
    }
    fun remove(key: String): SharePreferencesCache {
        editor.remove(key)
        return this
    }

    fun removeAll(): SharePreferencesCache {
        editor.clear()
        return this
    }

    fun getString(key: String, defValue: String): String = shared.getString(key, defValue) ?: ""
    fun getFloat(key: String, defValue: Float): Float = shared.getFloat(key, defValue)
    fun getLong(key: String, defValue: Long): Long = shared.getLong(key, defValue)
    fun getInt(key: String, defValue: Int): Int = shared.getInt(key, defValue)
    fun getBoolean(key: String, defValue: Boolean): Boolean = shared.getBoolean(key, defValue)
}