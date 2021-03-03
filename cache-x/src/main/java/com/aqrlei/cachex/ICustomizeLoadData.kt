package com.aqrlei.cachex

/**
 * created by AqrLei on 3/3/21
 */
fun interface ICustomizeLoadData {
    fun loadData(key: Key): ByteArray?
}