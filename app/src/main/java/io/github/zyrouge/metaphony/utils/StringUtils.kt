package io.github.zyrouge.metaphony.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal fun String.xDateToLocalDate(): LocalDate {
    val format = when (length) {
        4 -> DateTimeFormatter.ofPattern("yyyy")
        7 -> DateTimeFormatter.ofPattern("yyyy-MM")
        10 -> DateTimeFormatter.ofPattern("yyyy-MM-dd")
        else -> DateTimeFormatter.ISO_LOCAL_DATE
    }
    return LocalDate.parse(this, format)
}

internal fun String.xDateToLocalDateOrNull() = kotlin.runCatching { xDateToLocalDate() }.getOrNull()

internal fun String.xSplitToPairOrNull(delimiter: String): Pair<String, String>? {
    val values = split(delimiter, limit = 2)
    return when (values.size) {
        2 -> values[0] to values[1]
        else -> null
    }
}

internal fun String.xIntBeforeSlashOrNull() = xSplitToPairOrNull("/")?.first?.toIntOrNull()
internal fun String.xIntAfterSlashOrNull() = xSplitToPairOrNull("/")?.second?.toIntOrNull()
internal fun String.xIntBeforeSlashOrIntOrNull() = xIntBeforeSlashOrNull() ?: toIntOrNull()

internal fun String.withCase(sensitive: Boolean) = if (!sensitive) lowercase() else this
