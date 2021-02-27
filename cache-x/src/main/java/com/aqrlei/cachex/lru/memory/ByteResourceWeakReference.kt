package com.aqrlei.cachex.lru.memory

import com.aqrlei.cachex.Key
import java.lang.ref.ReferenceQueue
import java.lang.ref.WeakReference

/**
 * created by AqrLei on 2/26/21
 */
class ByteResourceWeakReference(
    val key: Key,
    private val referent: ByteResource,
    private val queue: ReferenceQueue<ByteResource>
): WeakReference<ByteResource>(referent,queue) {

    var resource: ByteArray? = if (referent.isMemoryCacheable) referent.getResource() else null
    val isCacheable:Boolean = referent.isMemoryCacheable

    fun reset() {
        resource = null
        clear()
    }
}