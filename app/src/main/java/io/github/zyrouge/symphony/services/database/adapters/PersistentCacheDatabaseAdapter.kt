package io.github.zyrouge.symphony.services.database.adapters

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class PersistentCacheDatabaseAdapter<T>(
    context: Context,
    val name: String,
    val version: Int,
    private val transformer: Transformer<T>,
) {
    private val helper = Helper(context, name, version)

    fun get(key: String): T? {
        val columns = arrayOf(COLUMN_VALUE)
        val selection = "$COLUMN_KEY = ?"
        val selectionArgs = arrayOf(key)
        helper.readableDatabase
            .query(name, columns, selection, selectionArgs, null, null, null)
            .use {
                val valueIndex = it.getColumnIndexOrThrow(COLUMN_VALUE)
                if (!it.moveToNext()) {
                    return null
                }
                val rawValue = it.getString(valueIndex)
                return transformer.deserialize(rawValue)
            }
    }

    fun put(key: String, value: T): Boolean {
        val values = ContentValues().apply {
            put(COLUMN_KEY, key)
            put(COLUMN_VALUE, transformer.serialize(value))
        }
        val conflict = SQLiteDatabase.CONFLICT_REPLACE
        val rowId = helper.writableDatabase.insertWithOnConflict(name, null, values, conflict)
        return rowId != -1L
    }

    fun delete(key: String): Boolean {
        val selection = "$COLUMN_KEY = ?"
        val selectionArgs = arrayOf(key)
        val count = helper.writableDatabase.delete(name, selection, selectionArgs)
        return count == 1
    }

    fun delete(keys: List<String>): Int {
        if (keys.isEmpty()) {
            return 0
        }
        val selectionPlaceholder = "?, ".repeat(keys.size).let {
            it.substring(it.length - 2, it.length)
        }
        val selection = "$COLUMN_KEY IN (${selectionPlaceholder})"
        val selectionArgs = keys.toTypedArray()
        val count = helper.writableDatabase.delete(name, selection, selectionArgs)
        return count
    }

    fun retain(keys: Collection<String>): Int {
        if (keys.isEmpty()) {
            return 0
        }
        val selectionPlaceholder = "?, ".repeat(keys.size).let {
            it.substring(it.length - 2, it.length)
        }
        val selection = "$COLUMN_KEY NOT IN (${selectionPlaceholder})"
        val selectionArgs = keys.toTypedArray()
        val count = helper.writableDatabase.delete(name, selection, selectionArgs)
        return count
    }

    fun clear(): Int {
        val count = helper.writableDatabase.delete(name, null, null)
        return count
    }

    fun keys(): List<String> {
        val keys = mutableListOf<String>()
        val columns = arrayOf(COLUMN_KEY)
        helper.readableDatabase
            .query(name, columns, null, null, null, null, null)
            .use {
                val keyIndex = it.getColumnIndexOrThrow(COLUMN_KEY)
                while (it.moveToNext()) {
                    val key = it.getString(keyIndex)
                    keys.add(key)
                }
            }
        return keys
    }

    fun all(): Map<String, T> {
        val all = mutableMapOf<String, T>()
        val columns = arrayOf(COLUMN_KEY)
        helper.readableDatabase
            .query(name, columns, null, null, null, null, null)
            .use {
                val keyIndex = it.getColumnIndexOrThrow(COLUMN_KEY)
                val valueIndex = it.getColumnIndexOrThrow(COLUMN_VALUE)
                while (it.moveToNext()) {
                    val key = it.getString(keyIndex)
                    val rawValue = it.getString(valueIndex)
                    val value = transformer.deserialize(rawValue)
                    all[key] = value
                }
            }
        return all
    }

    private class Helper(context: Context, val name: String, version: Int) :
        SQLiteOpenHelper(context, name, null, version) {
        override fun onCreate(db: SQLiteDatabase) {
            val query =
                "CREATE TABLE $name ($COLUMN_KEY TEXT PRIMARY KEY, $COLUMN_VALUE TEXT NOT NULL)"
            db.execSQL(query)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            val query = "DROP TABLE $name"
            db.execSQL(query)
            onCreate(db)
        }

        override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            onUpgrade(db, oldVersion, newVersion)
        }
    }

    abstract class Transformer<T> {
        abstract fun serialize(data: T): String
        abstract fun deserialize(data: String): T

        class AsString : Transformer<String>() {
            override fun serialize(data: String) = data
            override fun deserialize(data: String) = data
        }
    }

    companion object {
        const val COLUMN_KEY = "key"
        const val COLUMN_VALUE = "value"
    }
}