/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aqrlei.cachex.lru.disk

import java.io.*
import java.nio.charset.Charset

/** Junk drawer of utility methods. */
object Util {
    @JvmField
    val US_ASCII = Charset.forName("US-ASCII")

    @JvmField
    val UTF_8 = Charset.forName("UTF-8")

    @JvmStatic
    @Throws(IOException::class)
    fun readFully(reader: Reader): String {
        reader.use {
            val writer = StringWriter()
            val buffer = CharArray(1024)
            var count = 0
            while (count.also { count = reader.read(buffer) } != -1) {
                writer.write(buffer, 0, count)
            }
            return writer.toString()
        }
    }

    /**
     * Deletes the contents of {@code dir}. Throws an IOException if any file
     * could not be deleted, or if {@code dir} is not a readable directory.
     */
    @JvmStatic
    @Throws(IOException::class)
    fun deleteContents(dir: File) {
        val files = dir.listFiles() ?: throw IOException("not a readable directory: $dir")

        for (file in files) {
            if (file.isDirectory) {
                deleteContents(file)
            }
            if (!file.delete()) {
                throw IOException("not a readable directory: $dir")
            }
        }
    }

    @JvmStatic
    fun closeQuietly(closeable: Closeable?) {
        closeable?.run {
            try {
                close()
            } catch (rethrown: RuntimeException) {
                throw rethrown
            } catch (ignored: Exception) {
            }
        }
    }
}