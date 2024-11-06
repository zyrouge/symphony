package io.github.zyrouge.symphony.ui.helpers

import io.github.zyrouge.symphony.utils.SimpleFileSystem
import io.github.zyrouge.symphony.utils.SimplePath

fun SimpleFileSystem.Folder.navigateToFolder(path: SimplePath): SimpleFileSystem.Folder? {
    var folder: SimpleFileSystem.Folder? = this
    path.parts.forEach { x ->
        folder = folder?.let {
            val child = it.children[x]
            child as? SimpleFileSystem.Folder
        }
    }
    return folder
}
