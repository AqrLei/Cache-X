package com.aqrlei.cachex.lru.disklrucache

import java.io.*
import java.util.regex.Pattern

/**
 * created by AqrLei on 1/21/21
 */
class DiskLruCache private constructor(
    private val directory: File,
    private val appVersion: Int,
    private val valueCount: Int,
    private val maxSize: Long
) : Closeable {
    companion object {
        const val JOURNAL_FILE = "journal"
        const val JOURNAL_FILE_TEMP = "journal.tmp"
        const val JOURNAL_FILE_BACKUP = "journal.bkp"
        const val MAGIC = "libcore.io.DiskLruCache"
        const val VERSION_1 = "1"
        const val ANY_SEQUENCE_NUMBER: Long = -1
        val LEGAL_KEY_PATTERN = Pattern.compile("[a-z0-9_-]{1,64}")
        private const val CLEAN = "CLEAN"
        private const val DIRTY = "DIRTY"
        private const val REMOVE = "REMOVE"
        private const val READ = "READ"

        private val NULL_OUTPUT_STREAM = object: OutputStream() {
            override fun write(b: Int) {
                // Eat all writes silently. Nom nom.
            }
        }

        /**
         * Opens the cache in {@code directory}, creating a cache if none exists
         * there.
         *
         * @param directory a writable directory
         * @param valueCount the number of values per cache entry. Must be positive.
         * @param maxSize the maximum number of bytes this cache should use to store
         * @throws IOException if reading or writing the cache directory fails
         */
        @Throws(IOException::class)
        fun open(directory: File, appVersion: Int, valueCount: Int, maxSize: Long): DiskLruCache {
            require(maxSize > 0) { "maxSize must be greater than 0" }
            require(valueCount > 0) { "valueCount must be greater than 0" }


            // If a bkp file exists, use it instead.
            val backupFile = File(directory, JOURNAL_FILE_BACKUP)
            if (backupFile.exists()) {
                val journalFile = File(directory, JOURNAL_FILE)

                // If journal file also exists just delete backup file.
                if (journalFile.exists()) {
                    backupFile.delete()
                } else {
                    renameTo(backupFile, journalFile, false)
                }
            }

            // Prefer to pick up where we left off.
            var cache = DiskLruCache(directory, appVersion, valueCount, maxSize)
            if (cache.journalFile.exists()) {
                try {
                    cache.readJournal()
                    cache.processJournal()
                    cache.journalWriter = BufferedWriter(
                        OutputStreamWriter(FileOutputStream(cache.journalFile, true), Util.US_ASCII)
                    )
                    return cache
                } catch (journalIsCorrupt: IOException) {
                    println("DiskLruCache :$directory is corrupt: ${journalIsCorrupt.message}, removing")
                    cache.delete()
                }
            }

            // Create a new empty cache.
            directory.mkdirs()
            cache = DiskLruCache(directory, appVersion, valueCount, maxSize)
            cache.rebuildJournal()
            return cache
        }

        @Throws(IOException::class)
        private fun renameTo(from: File, to: File, deleteDestination: Boolean) {
            if (deleteDestination) {
                deleteIfExists(to)
            }
            if (!from.renameTo(to)) {
                throw IOException()
            }
        }

        @Throws(IOException::class)
        private fun deleteIfExists(file: File) {
            if (file.exists() && !file.delete()) {
                throw  IOException()
            }
        }
    }

    /*
    * This cache uses a journal file named "journal". A typical journal file
    * looks like this:
    *     libcore.io.DiskLruCache
    *     1
    *     100
    *     2
    *
    *     CLEAN 3400330d1dfc7f3f7f4b8d4d803dfcf6 832 21054
    *     DIRTY 335c4c6028171cfddfbaae1a9c313c52
    *     CLEAN 335c4c6028171cfddfbaae1a9c313c52 3934 2342
    *     REMOVE 335c4c6028171cfddfbaae1a9c313c52
    *     DIRTY 1ab96a171faeeee38496d8b330771a7a
    *     CLEAN 1ab96a171faeeee38496d8b330771a7a 1600 234
    *     READ 335c4c6028171cfddfbaae1a9c313c52
    *     READ 3400330d1dfc7f3f7f4b8d4d803dfcf6
    *
    * The first five lines of the journal form its header. They are the
    * constant string "libcore.io.DiskLruCache", the disk cache's version,
    * the application's version, the value count, and a blank line.
    *
    * Each of the subsequent lines in the file is a record of the state of a
    * cache entry. Each line contains space-separated values: a state, a key,
    * and optional state-specific values.
    *   o DIRTY lines track that an entry is actively being created or updated.
    *     Every successful DIRTY action should be followed by a CLEAN or REMOVE
    *     action. DIRTY lines without a matching CLEAN or REMOVE indicate that
    *     temporary files may need to be deleted.
    *   o CLEAN lines track a cache entry that has been successfully published
    *     and may be read. A publish line is followed by the lengths of each of
    *     its values.
    *   o READ lines track accesses for LRU.
    *   o REMOVE lines track entries that have been deleted.
    *
    * The journal file is appended to as cache operations occur. The journal may
    * occasionally be compacted by dropping redundant lines. A temporary file named
    * "journal.tmp" will be used during compaction; that file should be deleted if
    * it exists when the cache is opened.
    */

    private val journalFile = File(directory, JOURNAL_FILE)
    private val journalFileTmp = File(directory, JOURNAL_FILE_TEMP)
    private val journalFileBackup = File(directory, JOURNAL_FILE_BACKUP)

    private lateinit var journalWriter: BufferedWriter

    fun readJournal() {}

    fun processJournal() {}

    fun delete() {}

    fun rebuildJournal(){

    }

    override fun close() {
        TODO("Not yet implemented")
    }
}