package io.github.zyrouge.symphony.utils

import java.io.File

object FileX {
    fun ensureFile(file: File) {
        file.parentFile?.mkdirs()
        file.createNewFile()
    }
}
