package com.filemanager.app.data.repository

import android.os.Environment
import com.filemanager.app.domain.model.ClipboardItem
import com.filemanager.app.domain.model.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileRepository @Inject constructor() {

    private val clipboard = mutableListOf<ClipboardItem>()

    fun getRootPath(): String {
        return Environment.getExternalStorageDirectory().absolutePath
    }

    suspend fun getFiles(path: String): Result<List<FileItem>> = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            if (!file.exists() || !file.isDirectory) {
                return@withContext Result.failure(Exception("Invalid path"))
            }
            val files = file.listFiles()
                ?.filter { !it.isHidden }
                ?.map { FileItem.fromFile(it) }
                ?: emptyList()
            Result.success(files)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun copyToClipboard(items: List<FileItem>, isCut: Boolean) = withContext(Dispatchers.IO) {
        clipboard.clear()
        clipboard.addAll(items.map { ClipboardItem(it.path, it.isDirectory, isCut) })
    }

    fun getClipboardItems(): List<ClipboardItem> = clipboard.toList()

    fun hasClipboardContent(): Boolean = clipboard.isNotEmpty()

    fun clearClipboard() {
        clipboard.clear()
    }

    suspend fun paste(currentPath: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            var successCount = 0
            for (item in clipboard) {
                val sourceFile = File(item.sourcePath)
                val destFile = File(currentPath, sourceFile.name)

                if (destFile.exists()) {
                    continue
                }

                val result = if (item.isDirectory) {
                    copyDirectory(sourceFile, destFile)
                } else {
                    sourceFile.copyTo(destFile)
                    if (item.isCut) sourceFile.delete()
                    true
                }

                if (result) {
                    successCount++
                    if (item.isCut && sourceFile.parent != currentPath) {
                        deleteRecursive(sourceFile)
                    }
                }
            }
            clearClipboard()
            Result.success(successCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun copyDirectory(source: File, destination: File): Boolean {
        return try {
            if (!destination.exists()) {
                destination.mkdirs()
            }
            source.listFiles()?.forEach { file ->
                val destFile = File(destination, file.name)
                if (file.isDirectory) {
                    copyDirectory(file, destFile)
                } else {
                    file.copyTo(destFile)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun delete(item: FileItem): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val result = if (item.isDirectory) {
                deleteRecursive(item.file)
            } else {
                item.file.delete()
            }
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun deleteRecursive(file: File): Boolean {
        return try {
            if (file.isDirectory) {
                file.listFiles()?.forEach { deleteRecursive(it) }
            }
            file.delete()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun rename(item: FileItem, newName: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val newFile = File(item.file.parentFile, newName)
            if (newFile.exists()) {
                return@withContext Result.failure(Exception("File already exists"))
            }
            val success = item.file.renameTo(newFile)
            if (success) {
                Result.success(newFile)
            } else {
                Result.failure(Exception("Rename failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createFolder(path: String, name: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val newFolder = File(path, name)
            if (newFolder.exists()) {
                return@withContext Result.failure(Exception("Folder already exists"))
            }
            val success = newFolder.mkdir()
            if (success) {
                Result.success(newFolder)
            } else {
                Result.failure(Exception("Failed to create folder"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
