package com.fileflow.domain.model

import kotlinx.serialization.Serializable

enum class Category(val key: String) {
    Images("Images"), Videos("Videos"), Audio("Audio"), Documents("Documents"),
    Archives("Archives"), Installers("Installers"), Development("Development"),
    Models3D("3D Models"), Fonts("Fonts"), Torrents("Torrents"), Other("Other")
}

data class FileEntry(
    val path: String,
    val name: String,
    val extension: String,
    val size: Long,
    val createdAt: Long?,
    val modifiedAt: Long?,
    val isHidden: Boolean,
    val mimeType: String? = null
)

@Serializable
data class MovePlanItem(
    val sourcePath: String,
    val targetPath: String,
    val category: String,
    val size: Long
)

data class DuplicateGroup(
    val hash: String,
    val size: Long,
    val files: List<FileEntry>
) {
    val copies: Int get() = files.size
    val reclaimable: Long get() = size * (files.size - 1)
}

data class SortRule(
    val id: Long,
    val name: String,
    val enabled: Boolean,
    val priority: Int,
    val extension: String? = null,
    val nameContains: String? = null,
    val olderThanDays: Int? = null,
    val targetSubdir: String,
    val categoryOverride: Category? = null
)

data class MoveRecord(val sourcePath: String, val targetPath: String)

data class OperationRecord(
    val id: Long,
    val type: String,
    val timestamp: Long,
    val sourceDir: String,
    val targetDir: String,
    val count: Int,
    val status: String,
    val moves: List<MoveRecord>
)

data class ScanProgress(val scanned: Int, val totalEstimate: Int? = null, val currentPath: String = "")

data class ScanResult(val files: List<FileEntry>, val totalSize: Long)

@kotlinx.serialization.Serializable
data class Settings(
    val darkTheme: Boolean = true,
    val language: String = "ru",
    val defaultTargetDir: String = "",
    val conflictResolution: String = "rename",
    val excludedDirs: List<String> = emptyList(),
    val minLargeFileSizeMb: Long = 100,
    val scanHidden: Boolean = false,
    val followSymlinks: Boolean = false
)