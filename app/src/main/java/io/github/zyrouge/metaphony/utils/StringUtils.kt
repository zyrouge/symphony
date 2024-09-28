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

private fun parseSlashSeparatedNumbers(value: String): Pair<Int, Int>? {
    val split = value.split("/")
    if (split.size == 2) {
        return split[0].toInt() to split[1].toInt()
    }
    return null
}

internal fun String.xIntBeforeSlash() = parseSlashSeparatedNumbers(this)?.first
internal fun String.xIntAfterSlash() = parseSlashSeparatedNumbers(this)?.second
