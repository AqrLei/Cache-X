package com.aqrlei.cachex.lru.task

import com.aqrlei.cachex.lru.ICacheTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

/**
 * Created by AqrLei on 2019-06-27
 */
class SimpleCacheTask : ICacheTask {
    override fun doInBackground(taskName: String, groupName: String, onBackground: () -> Unit) =
        runBlocking(Dispatchers.IO) {
            onBackground()
        }
}