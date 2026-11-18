package io.github.zyrouge.symphony.services.groove

fun parseM3u(content: String): List<String> = content.lineSequence()
    .map { it.trim() }
    .filter { it.isNotEmpty() && it[0] != '#' }
    .toList()

fun buildM3u(songPaths: List<String>): String = songPaths.joinToString("\n")
