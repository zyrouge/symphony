package io.github.zyrouge.symphony.ui.helpers

import io.github.zyrouge.symphony.utils.SimpleFileSystem

fun SimpleFileSystem.Folder.navigateToFolder(parts: List<String>): SimpleFileSystem.Folder? {
    var folder: SimpleFileSystem.Folder? = this
    parts.forEach { part ->
        folder = folder?.let {
            val child = it.children[part]
            child as? SimpleFileSystem.Folder
        }
    }
    return folder
}
