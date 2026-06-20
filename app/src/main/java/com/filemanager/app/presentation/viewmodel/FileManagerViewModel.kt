package com.filemanager.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filemanager.app.data.repository.FileRepository
import com.filemanager.app.domain.model.ClipboardItem
import com.filemanager.app.domain.model.FileItem
import com.filemanager.app.domain.model.SortType
import com.filemanager.app.domain.model.ViewMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class FileManagerUiState(
    val currentPath: String = "",
    val files: List<FileItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedItems: Set<String> = emptySet(),
    val isSelectionMode: Boolean = false,
    val clipboardItems: List<ClipboardItem> = emptyList(),
    val hasClipboard: Boolean = false,
    val sortType: SortType = SortType.NAME,
    val viewMode: ViewMode = ViewMode.LIST,
    val pathHistory: List<String> = emptyList()
)

@HiltViewModel
class FileManagerViewModel @Inject constructor(
    private val repository: FileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FileManagerUiState())
    val uiState: StateFlow<FileManagerUiState> = _uiState.asStateFlow()

    init {
        navigateToPath(repository.getRootPath())
    }

    fun navigateToPath(path: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, selectedItems = emptySet(), isSelectionMode = false) }

            repository.getFiles(path).fold(
                onSuccess = { files ->
                    val sortedFiles = sortFiles(files, _uiState.value.sortType)
                    _uiState.update {
                        it.copy(
                            currentPath = path,
                            files = sortedFiles,
                            isLoading = false,
                            pathHistory = it.pathHistory + path
                        )
                    }
                    updateClipboardState()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
            )
        }
    }

    fun navigateUp(): Boolean {
        val currentPath = _uiState.value.currentPath
        val parentPath = File(currentPath).parentFile?.absolutePath ?: return false
        if (parentPath == currentPath) return false
        navigateToPath(parentPath)
        return true
    }

    fun openFolder(item: FileItem) {
        if (item.isDirectory) {
            navigateToPath(item.path)
        }
    }

    fun toggleSelection(item: FileItem) {
        _uiState.update { state ->
            val newSelected = if (state.selectedItems.contains(item.path)) {
                state.selectedItems - item.path
            } else {
                state.selectedItems + item.path
            }
            state.copy(
                selectedItems = newSelected,
                isSelectionMode = newSelected.isNotEmpty()
            )
        }
    }

    fun selectAll() {
        _uiState.update { state ->
            state.copy(
                selectedItems = state.files.map { it.path }.toSet(),
                isSelectionMode = true
            )
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedItems = emptySet(), isSelectionMode = false) }
    }

    fun copySelected() {
        val selectedFiles = _uiState.value.files.filter { _uiState.value.selectedItems.contains(it.path) }
        viewModelScope.launch {
            repository.copyToClipboard(selectedFiles, isCut = false)
            updateClipboardState()
            clearSelection()
        }
    }

    fun cutSelected() {
        val selectedFiles = _uiState.value.files.filter { _uiState.value.selectedItems.contains(it.path) }
        viewModelScope.launch {
            repository.copyToClipboard(selectedFiles, isCut = true)
            updateClipboardState()
            clearSelection()
        }
    }

    fun paste() {
        viewModelScope.launch {
            val result = repository.paste(_uiState.value.currentPath)
            result.fold(
                onSuccess = { refreshCurrentDirectory() },
                onFailure = { error -> _uiState.update { it.copy(error = error.message) } }
            )
            updateClipboardState()
        }
    }

    fun deleteSelected() {
        val selectedFiles = _uiState.value.files.filter { _uiState.value.selectedItems.contains(it.path) }
        viewModelScope.launch {
            selectedFiles.forEach { item ->
                repository.delete(item)
            }
            clearSelection()
            refreshCurrentDirectory()
        }
    }

    fun renameItem(item: FileItem, newName: String) {
        viewModelScope.launch {
            val result = repository.rename(item, newName)
            result.fold(
                onSuccess = { refreshCurrentDirectory() },
                onFailure = { error -> _uiState.update { it.copy(error = error.message) } }
            )
        }
    }

    fun createFolder(name: String) {
        viewModelScope.launch {
            val result = repository.createFolder(_uiState.value.currentPath, name)
            result.fold(
                onSuccess = { refreshCurrentDirectory() },
                onFailure = { error -> _uiState.update { it.copy(error = error.message) } }
            )
        }
    }

    fun setSortType(sortType: SortType) {
        _uiState.update { state ->
            state.copy(
                sortType = sortType,
                files = sortFiles(state.files, sortType)
            )
        }
    }

    fun toggleViewMode() {
        _uiState.update { state ->
            state.copy(
                viewMode = if (state.viewMode == ViewMode.LIST) ViewMode.GRID else ViewMode.LIST
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun updateClipboardState() {
        _uiState.update {
            it.copy(
                clipboardItems = repository.getClipboardItems(),
                hasClipboard = repository.hasClipboardContent()
            )
        }
    }

    private fun refreshCurrentDirectory() {
        navigateToPath(_uiState.value.currentPath)
    }

    private fun sortFiles(files: List<FileItem>, sortType: SortType): List<FileItem> {
        val folders = files.filter { it.isDirectory }
        val regularFiles = files.filter { !it.isDirectory }

        val sortedFolders = when (sortType) {
            SortType.NAME -> folders.sortedBy { it.name.lowercase() }
            SortType.SIZE -> folders.sortedByDescending { it.size }
            SortType.DATE -> folders.sortedByDescending { it.lastModified }
        }

        val sortedFiles = when (sortType) {
            SortType.NAME -> regularFiles.sortedBy { it.name.lowercase() }
            SortType.SIZE -> regularFiles.sortedByDescending { it.size }
            SortType.DATE -> regularFiles.sortedByDescending { it.lastModified }
        }

        return sortedFolders + sortedFiles
    }
}
