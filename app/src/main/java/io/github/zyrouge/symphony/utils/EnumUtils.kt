package io.github.zyrouge.symphony.utils

import android.content.SharedPreferences

inline fun <reified T : Enum<T>> SharedPreferences.getEnum(
    key: String,
    defaultValue: T?,
) = getString(key, null)?.let { parseEnumValue<T>(it) } ?: defaultValue

inline fun <reified T : Enum<T>> SharedPreferences.Editor.putEnum(
    key: String,
    value: T?,
) = putString(key, value?.name)

inline fun <reified T : Enum<T>> parseEnumValue(value: String): T? =
    T::class.java.enumConstants?.find { it.name == value }

