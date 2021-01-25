package com.aqrlei.cachex.lru.disklrucache

import com.aqrlei.cachex.lru.disklrucache.Util.closeQuietly
import java.io.*
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.LinkedHashMap

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

        @Throws(IOException::class)
        private fun inputStreamToString(inputStream: InputStream): String {
            return Util.readFully(InputStreamReader(inputStream, Util.UTF_8))
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

    private  var journalWriter: BufferedWriter?= null

    //TODO why
    private val lruEntries = LinkedHashMap<String, Entry>(0, 0.75F, true)

    @Throws(IOException::class)
    private fun readJournal() {}

    @Throws(IOException::class)
    fun processJournal() {}

    @Throws(IOException::class)
    fun delete() {}

    @Synchronized
    @Throws(IOException::class)
    private fun rebuildJournal(){

    }

    @Throws(IOException::class)
    @Synchronized
    private fun completeEdit(editor: Editor, success: Boolean) {

    }

    @Throws(IOException::class)
    @Synchronized
    private fun remove(key: String) {

    }

    @Throws(IOException::class)
    @Synchronized
    private fun edit(key: String, expectedSequenceNumber: Long):Editor {
        TODO()
    }


    override fun close() {
        journalWriter?:return

    }

    /** A snapshot of the values for an entry. */
    inner class SnapShot(
        private val key: String,
        private val sequenceNumber: Long,
        private val ins : Array<InputStream>,
        private val lengths: LongArray
    ): Closeable {

        /**
         * Returns an editor for this snapshot's entry, or null if either the
         * entry has changed since this snapshot was created or if another edit
         * is in progress.
         */
        @Throws(IOException::class)
        fun edit(): Editor {
            return this@DiskLruCache.edit(key, sequenceNumber)
        }

        /** Returns the unbuffered stream with the value for `index`.  */
        fun getInputStream(index: Int): InputStream {
            return ins[index]
        }

        /** Returns the string value for `index`.  */
        @Throws(IOException::class)
        fun getString(index: Int): String {
            return inputStreamToString(getInputStream(index))
        }

        /** Returns the byte length of the value for `index`.  */
        fun getLength(index: Int): Long {
            return lengths[index]
        }

        override fun close() {
            for (input in ins) {
                closeQuietly(input)
            }
        }
    }

    inner class Entry(val key: String) {
        /** Lengths of this entry's files.  */
        private var lengths: LongArray = LongArray(valueCount)

        /** True if this entry has ever been published.  */
        var readable : Boolean = false

        /** The ongoing edit or null if this entry is not being edited.  */
        var currentEditor: Editor? = null

        /** The sequence number of the most recently committed edit to this entry.  */
        private var sequenceNumber: Long = 0

        @Throws(IOException::class)
        fun getLengths(): String {
            val result = StringBuilder()
            for (size in lengths) {
                result.append(' ').append(size)
            }
            return result.toString()
        }

        /** Set lengths using decimal numbers like "10123".  */
        @Throws(IOException::class)
        private fun setLengths(strings: Array<String>) {
            if (strings.size != valueCount) {
                throw invalidLengths(strings)
            }
            try {
                for (i in strings.indices) {
                    lengths[i] = strings[i].toLong()
                }
            } catch (e: NumberFormatException) {
                throw invalidLengths(strings)
            }
        }

        @Throws(IOException::class)
        private fun invalidLengths(strings: Array<String>): IOException {
            throw IOException("unexpected journal line: " + strings.contentToString())
        }

        fun getCleanFile(i: Int): File {
            return File(directory, "$key.$i")
        }

        fun getDirtyFile(i: Int): File {
            return File(directory, "$key.$i.tmp")
        }

    }

    /** Edits the values for an entry. */
   inner class Editor(val entry: Entry) {

        private val written: BooleanArray? = if (entry.readable) null else BooleanArray(valueCount)
        private var hasErrors = false
        private var committed = false


        /**
         * Returns an unbuffered input stream to read the last committed value,
         * or null if no value has been committed.
         */
        @Throws(IOException::class)
        fun newInputStream(index: Int): InputStream? {
            synchronized(this@DiskLruCache) {
                check(entry.currentEditor == this)
                return if (!entry.readable) {
                    null
                } else try {
                    FileInputStream(entry.getCleanFile(index))
                } catch (e: FileNotFoundException) {
                    null
                }
            }
        }

        /**
         * Returns the last committed value as a string, or null if no value
         * has been committed.
         */
        @Throws(IOException::class)
        fun getString(index: Int): String? {
            val inputStream = newInputStream(index)
            return if (inputStream != null) inputStreamToString(inputStream) else null
        }

        /**
         * Returns a new unbuffered output stream to write the value at
         * `index`. If the underlying output stream encounters errors
         * when writing to the filesystem, this edit will be aborted when
         * [.commit] is called. The returned output stream does not throw
         * IOExceptions.
         */
        @Throws(IOException::class)
        fun newOutputStream(index: Int): OutputStream {
            synchronized(this@DiskLruCache) {
                check(entry.currentEditor == this)
                if (!entry.readable) {
                    written?.set(index, true)
                }
                val dirtyFile = entry.getDirtyFile(index)
                val outputStream: FileOutputStream
                outputStream = try {
                    FileOutputStream(dirtyFile)
                } catch (e: FileNotFoundException) {
                    // Attempt to recreate the cache directory.
                    directory.mkdirs()
                    try {
                        FileOutputStream(dirtyFile)
                    } catch (e2: FileNotFoundException) {
                        // We are unable to recover. Silently eat the writes.
                        return NULL_OUTPUT_STREAM
                    }
                }
                return FaultHidingOutputStream(outputStream)
            }
        }

        /** Sets the value at `index` to `value`.  */
        @Throws(IOException::class)
        operator fun set(index: Int, value: String?) {
            var writer: Writer? = null
            try {
                writer = OutputStreamWriter(newOutputStream(index), Util.UTF_8)
                writer.write(value)
            } finally {
                closeQuietly(writer)
            }
        }

        /**
         * Commits this edit so it is visible to readers.  This releases the
         * edit lock so another edit may be started on the same key.
         */
        @Throws(IOException::class)
        fun commit() {
            if (hasErrors) {
                completeEdit(this, false)
                remove(entry.key) // The previous entry is stale.
            } else {
                completeEdit(this, true)
            }
            committed = true
        }

        /**
         * Aborts this edit. This releases the edit lock so another edit may be
         * started on the same key.
         */
        @Throws(IOException::class)
        fun abort() {
            completeEdit(this, false)
        }

        fun abortUnlessCommitted() {
            if (!committed) {
                try {
                    abort()
                } catch (ignored: IOException) {
                }
            }
        }

        private inner class FaultHidingOutputStream constructor(out: OutputStream) :
            FilterOutputStream(out) {
            override fun write(oneByte: Int) {
                try {
                    out.write(oneByte)
                } catch (e: IOException) {
                    hasErrors = true
                }
            }

            override fun write(buffer: ByteArray, offset: Int, length: Int) {
                try {
                    out.write(buffer, offset, length)
                } catch (e: IOException) {
                    hasErrors = true
                }
            }

            override fun close() {
                try {
                    out.close()
                } catch (e: IOException) {
                    hasErrors = true
                }
            }

            override fun flush() {
                try {
                    out.flush()
                } catch (e: IOException) {
                    hasErrors = true
                }
            }
        }
    }

}