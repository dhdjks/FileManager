package com.filemanager.app.domain.model

import java.io.File

data class FileItem(
    val file: File,
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long,
    val extension: String = ""
) {
    companion object {
        fun fromFile(file: File): FileItem {
            return FileItem(
                file = file,
                name = file.name,
                path = file.absolutePath,
                isDirectory = file.isDirectory,
                size = if (file.isDirectory) 0L else file.length(),
                lastModified = file.lastModified(),
                extension = if (file.isDirectory) "" else file.extension.lowercase()
            )
        }
    }
}

enum class SortType {
    NAME, SIZE, DATE
}

enum class ViewMode {
    LIST, GRID
}
