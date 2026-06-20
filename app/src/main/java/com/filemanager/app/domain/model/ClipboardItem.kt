package com.filemanager.app.domain.model

data class ClipboardItem(
    val sourcePath: String,
    val isDirectory: Boolean,
    val isCut: Boolean
)
