package com.aqrlei.cachex.lru.disk

import java.util.concurrent.locks.ReentrantLock

/**
 * created by AqrLei on 3/3/21
 */
class DiskCacheWriteLocker {

    private val locks = HashMap<String, WriteLock>()
    private val writeLockPool = WriteLockPool()

    fun acquire(safeKey: String) {
        var writeLock: WriteLock?
        synchronized(this) {
            writeLock = locks[safeKey]
            if (writeLock == null) {
                writeLock = writeLockPool.obtain()
                locks[safeKey] = writeLock!!
            }
            writeLock!!.interestedThreads++
        }
        writeLock?.lock?.lock()
    }

    fun release(safeKey: String) {
        var writeLock: WriteLock
        synchronized(this) {
            writeLock = checkNotNull(locks[safeKey]) { "arguments must not be null" }

            check(writeLock.interestedThreads > 1) {
                "Cannot release a lock that is not held, safeKey: ${safeKey}, interestedThreads: ${writeLock.interestedThreads}"
            }

            writeLock.interestedThreads--
            if (writeLock.interestedThreads == 0) {
                val removed = locks.remove(safeKey)
                check(writeLock == removed) {
                    "Removed the wrong lock, expected to remove ${writeLock}, but actually removed: ${removed}, safeKey: $safeKey"
                }
            }
        }
        writeLock.lock.unlock()
    }

    private class WriteLock {
        val lock = ReentrantLock()
        var interestedThreads: Int = 0
    }

    private class WriteLockPool {
        companion object {
            private const val MAX_POOL_SIZE = 10
        }

        private val pool = ArrayDeque<WriteLock>()

        fun obtain(): WriteLock {
            var result: WriteLock?
            synchronized(pool) {
                result = pool.removeFirstOrNull()
            }
            if (result == null) {
                result = WriteLock()
            }
            return result!!
        }

        fun offer(writeLock: WriteLock) {
            synchronized(pool) {
                if (pool.size < MAX_POOL_SIZE) {
                    pool.addLast(writeLock)
                }
            }
        }
    }
}