package com.aqrlei.litecache.lru

import androidx.annotation.WorkerThread

/**
 * Created by AqrLei on 2019-06-28
 */
interface ICacheTask {
    @WorkerThread
    fun doInBackground(
        taskName: String,
        groupName: String = "default_group",
        onBackground: () -> Unit)
}