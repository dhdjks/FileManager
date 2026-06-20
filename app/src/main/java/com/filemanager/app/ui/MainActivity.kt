package com.filemanager.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Paste
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.SortByAttributes
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.filemanager.app.domain.model.FileItem
import com.filemanager.app.domain.model.SortType
import com.filemanager.app.domain.model.ViewMode
import com.filemanager.app.presentation.viewmodel.FileManagerViewModel
import com.filemanager.app.ui.theme.ApkGreen
import com.filemanager.app.ui.theme.AudioGreen
import com.filemanager.app.ui.theme.DocBlue
import com.filemanager.app.ui.theme.FileGray
import com.filemanager.app.ui.theme.FolderYellow
import com.filemanager.app.ui.theme.ImageBlue
import com.filemanager.app.ui.theme.PdfRed
import com.filemanager.app.ui.theme.VideoRed
import com.filemanager.app.ui.theme.ZipPurple
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FileManagerApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileManagerApp(
    viewModel: FileManagerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf<FileItem?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showFileOptionsSheet by remember { mutableStateOf<FileItem?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermission = permissions.values.all { it }
        if (hasPermission) {
            viewModel.navigateToPath(Environment.getExternalStorageDirectory().absolutePath)
        }
    }

    LaunchedEffect(Unit) {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            arrayOf(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }

        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            hasPermission = true
        } else {
            permissionLauncher.launch(permissions)
        }
    }

    FileManagerTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = uiState.currentPath.split("/").lastOrNull() ?: "文件管理器",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.navigateUp() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.navigateToPath(uiState.currentPath) }) {
                            Icon(Icons.Default.Refresh, contentDescription = "刷新")
                        }
                        Box {
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(Icons.Default.SortByAlpha, contentDescription = "排序")
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("按名称排序") },
                                    onClick = {
                                        viewModel.setSortType(SortType.NAME)
                                        showSortMenu = false
                                    },
                                    leadingIcon = { Icon(Icons.Default.SortByAlpha, null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("按大小排序") },
                                    onClick = {
                                        viewModel.setSortType(SortType.SIZE)
                                        showSortMenu = false
                                    },
                                    leadingIcon = { Icon(Icons.Default.SortByAttributes, null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("按时间排序") },
                                    onClick = {
                                        viewModel.setSortType(SortType.DATE)
                                        showSortMenu = false
                                    },
                                    leadingIcon = { Icon(Icons.Default.SdStorage, null) }
                                )
                            }
                        }
                        IconButton(onClick = { viewModel.toggleViewMode() }) {
                            Icon(
                                if (uiState.viewMode == ViewMode.LIST) Icons.Default.GridView else Icons.Default.ViewList,
                                contentDescription = "切换视图"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showCreateFolderDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Folder, contentDescription = "新建文件夹", tint = Color.White)
                }
            },
            bottomBar = {
                AnimatedVisibility(
                    visible = uiState.hasClipboard || uiState.isSelectionMode,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shadowElevation = 8.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            if (uiState.isSelectionMode) {
                                IconButton(onClick = { viewModel.copySelected() }) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "复制")
                                        Text("复制", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                IconButton(onClick = { viewModel.cutSelected() }) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.ContentCut, contentDescription = "剪切")
                                        Text("剪切", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                IconButton(onClick = { viewModel.deleteSelected() }) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error)
                                        Text("删除", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                                    }
                                }
                                TextButton(onClick = { viewModel.clearSelection() }) {
                                    Text("取消")
                                }
                            } else if (uiState.hasClipboard) {
                                IconButton(onClick = { viewModel.paste() }) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Paste, contentDescription = "粘贴", tint = MaterialTheme.colorScheme.primary)
                                        Text("粘贴", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    uiState.error != null -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = uiState.error ?: "Unknown error",
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            TextButton(onClick = { viewModel.clearError() }) {
                                Text("重试")
                            }
                        }
                    }
                    uiState.files.isEmpty() -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Folder,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = FileGray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "文件夹为空",
                                style = MaterialTheme.typography.bodyLarge,
                                color = FileGray
                            )
                        }
                    }
                    uiState.viewMode == ViewMode.LIST -> {
                        FileListView(
                            files = uiState.files,
                            selectedItems = uiState.selectedItems,
                            isSelectionMode = uiState.isSelectionMode,
                            onItemClick = { item ->
                                if (uiState.isSelectionMode) {
                                    viewModel.toggleSelection(item)
                                } else {
                                    viewModel.openFolder(item)
                                }
                            },
                            onItemLongClick = { item ->
                                viewModel.toggleSelection(item)
                            },
                            onOptionsClick = { item ->
                                showFileOptionsSheet = item
                            }
                        )
                    }
                    else -> {
                        FileGridView(
                            files = uiState.files,
                            selectedItems = uiState.selectedItems,
                            isSelectionMode = uiState.isSelectionMode,
                            onItemClick = { item ->
                                if (uiState.isSelectionMode) {
                                    viewModel.toggleSelection(item)
                                } else {
                                    viewModel.openFolder(item)
                                }
                            },
                            onItemLongClick = { item ->
                                viewModel.toggleSelection(item)
                            }
                        )
                    }
                }
            }
        }

        if (showCreateFolderDialog) {
            CreateFolderDialog(
                onDismiss = { showCreateFolderDialog = false },
                onCreate = { name ->
                    viewModel.createFolder(name)
                    showCreateFolderDialog = false
                }
            )
        }

        showRenameDialog?.let { item ->
            RenameDialog(
                currentName = item.name,
                onDismiss = { showRenameDialog = null },
                onRename = { newName ->
                    viewModel.renameItem(item, newName)
                    showRenameDialog = null
                }
            )
        }

        showFileOptionsSheet?.let { item ->
            FileOptionsBottomSheet(
                item = item,
                onDismiss = { showFileOptionsSheet = null },
                onRename = {
                    showFileOptionsSheet = null
                    showRenameDialog = item
                },
                onDelete = {
                    viewModel.toggleSelection(item)
                    viewModel.deleteSelected()
                    showFileOptionsSheet = null
                }
            )
        }
    }
}

@Composable
fun FileListView(
    files: List<FileItem>,
    selectedItems: Set<String>,
    isSelectionMode: Boolean,
    onItemClick: (FileItem) -> Unit,
    onItemLongClick: (FileItem) -> Unit,
    onOptionsClick: (FileItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(files, key = { it.path }) { file ->
            FileListItem(
                file = file,
                isSelected = selectedItems.contains(file.path),
                isSelectionMode = isSelectionMode,
                onClick = { onItemClick(file) },
                onLongClick = { onItemLongClick(file) },
                onOptionsClick = { onOptionsClick(file) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileListItem(
    file: FileItem,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onOptionsClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (file.isDirectory) FolderYellow.copy(alpha = 0.2f)
                        else getFileColor(file.extension).copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (file.isDirectory) Icons.Default.Folder
                    else getFileIcon(file.extension),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = if (file.isDirectory) FolderYellow else getFileColor(file.extension)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (file.isDirectory) "文件夹" else formatFileSize(file.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Text(
                text = formatDate(file.lastModified),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )

            if (isSelectionMode) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Text("✓", color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                }
            } else {
                IconButton(onClick = onOptionsClick) {
                    Icon(
                        Icons.Default.Folder,
                        contentDescription = "选项",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun FileGridView(
    files: List<FileItem>,
    selectedItems: Set<String>,
    isSelectionMode: Boolean,
    onItemClick: (FileItem) -> Unit,
    onItemLongClick: (FileItem) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(files, key = { it.path }) { file ->
            FileGridItem(
                file = file,
                isSelected = selectedItems.contains(file.path),
                isSelectionMode = isSelectionMode,
                onClick = { onItemClick(file) },
                onLongClick = { onItemLongClick(file) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileGridItem(
    file: FileItem,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (file.isDirectory) FolderYellow.copy(alpha = 0.2f)
                        else getFileColor(file.extension).copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (file.isDirectory) Icons.Default.Folder
                    else getFileIcon(file.extension),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = if (file.isDirectory) FolderYellow else getFileColor(file.extension)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = file.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (isSelectionMode) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Text("✓", color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileOptionsBottomSheet(
    item: FileItem,
    onDismiss: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )

            TextButton(
                onClick = onRename,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("重命名")
                }
            }

            TextButton(
                onClick = onDelete,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun CreateFolderDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var folderName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建文件夹") },
        text = {
            TextField(
                value = folderName,
                onValueChange = { folderName = it },
                label = { Text("文件夹名称") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (folderName.isNotBlank()) onCreate(folderName) },
                enabled = folderName.isNotBlank()
            ) {
                Text("创建")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun RenameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit
) {
    var newName by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("重命名") },
        text = {
            TextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("新名称") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (newName.isNotBlank() && newName != currentName) onRename(newName) },
                enabled = newName.isNotBlank() && newName != currentName
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

fun getFileIcon(extension: String): ImageVector {
    return when (extension.lowercase()) {
        "jpg", "jpeg", "png", "gif", "bmp", "webp" -> Icons.Default.Image
        "mp4", "avi", "mkv", "mov", "wmv" -> Icons.Default.VideoFile
        "mp3", "wav", "flac", "aac", "ogg" -> Icons.Default.MusicNote
        "pdf" -> Icons.Default.Description
        "doc", "docx", "txt", "rtf" -> Icons.Default.Description
        else -> Icons.Default.Description
    }
}

fun getFileColor(extension: String): Color {
    return when (extension.lowercase()) {
        "jpg", "jpeg", "png", "gif", "bmp", "webp" -> ImageBlue
        "mp4", "avi", "mkv", "mov", "wmv" -> VideoRed
        "mp3", "wav", "flac", "aac", "ogg" -> AudioGreen
        "pdf" -> PdfRed
        "doc", "docx", "txt", "rtf" -> DocBlue
        "apk" -> ApkGreen
        "zip", "rar", "7z", "tar", "gz" -> ZipPurple
        else -> FileGray
    }
}

fun formatFileSize(size: Long): String {
    if (size <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format(
        Locale.getDefault(),
        "%.1f %s",
        size / Math.pow(1024.0, digitGroups.toDouble()),
        units[digitGroups]
    )
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MM/dd", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
