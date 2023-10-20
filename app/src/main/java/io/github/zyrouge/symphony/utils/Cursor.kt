package io.github.zyrouge.symphony.utils

import android.database.Cursor

fun Cursor.getColumnIndices(columns: List<String>) = columns.associateWith {
    getColumnIndex(it)
}

data class CursorShorty(
    val cursor: Cursor,
    val indices: Map<String, Int>
) {
    fun getInt(column: String) = cursor.getInt(indices[column]!!)
    fun getLong(column: String) = cursor.getLong(indices[column]!!)
    fun getString(column: String): String = cursor.getString(indices[column]!!)

    fun getIntNullable(column: String): Int? {
        val idx = indices[column]!!
        return if (idx > -1) cursor.getInt(idx) else null
    }

    fun getStringNullable(column: String): String? {
        val idx = indices[column]!!
        return if (idx > -1) cursor.getString(idx) else null
    }
}
