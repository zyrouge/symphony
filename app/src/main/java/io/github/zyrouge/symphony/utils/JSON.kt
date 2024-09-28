package io.github.zyrouge.symphony.utils

import org.json.JSONArray
import org.json.JSONObject


fun JSONObject.getStringOrNull(key: String): String? = if (has(key)) getString(key) else null

fun JSONObject.getIntOrNull(key: String): Int? = if (has(key)) getInt(key) else null

fun JSONObject.getLongOrNull(key: String): Long? = if (has(key)) getLong(key) else null

fun JSONObject.getJSONArrayOrNull(key: String): JSONArray? =
    if (has(key)) getJSONArray(key) else null

private typealias _ArrayCollector<T> = JSONArray.(Int) -> T

fun <U, V : MutableCollection<U>> JSONArray.collectInto(into: V, fn: _ArrayCollector<U>): V {
    for (i in 0 until length()) {
        into.add(fn.invoke(this, i))
    }
    return into
}

fun <T> JSONArray.toList(fn: _ArrayCollector<T>): List<T> = collectInto(mutableListOf()) { fn(it) }

fun <T> JSONArray.toSet(fn: _ArrayCollector<T>): Set<T> = collectInto(mutableSetOf()) { fn(it) }

fun JSONObject.getStringOrEmptySet(key: String) =
    getJSONArrayOrNull(key)?.toSet { getString(it) } ?: emptySet()
