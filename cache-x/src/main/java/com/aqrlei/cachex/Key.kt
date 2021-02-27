package com.aqrlei.cachex

import java.nio.charset.Charset

/**
 * created by AqrLei on 2/26/21
 */
interface Key {
    companion object {
        val STRING_CHARSET_NAME = "UTF-8"
        val CHARSET = Charset.forName(STRING_CHARSET_NAME)
    }

    override fun equals(other: Any?): Boolean

    override fun hashCode(): Int
}