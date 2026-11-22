package com.fileflow.data.fs

import kotlinx.coroutines.ensureActive

import com.fileflow.domain.model.FileEntry
import com.fileflow.domain.model.ScanProgress
import com.fileflow.domain.model.ScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime

class FileScanner {
    fun scan(
        root: Path,
        recursive: Boolean = true,
        includeHidden: Boolean = false,
        followSymlinks: Boolean = false,
        excluded: Set<Path> = emptySet()
    ): Flow<ScanProgress> = flow {
        val collected = mutableListOf<FileEntry>()
        var scanned = 0
        val visitor = object : SimpleFileVisitor<Path>() {
            override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                if (dir != root && !includeHidden && isHidden(dir)) return FileVisitResult.SKIP_SUBTREE
                if (excluded.any { dir.startsWith(it) }) return FileVisitResult.SKIP_SUBTREE
                if (!followSymlinks && attrs.isSymbolicLink) return FileVisitResult.SKIP_SUBTREE
                return FileVisitResult.CONTINUE
            }
            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                if (!includeHidden && isHidden(file)) return FileVisitResult.CONTINUE
                if (excluded.any { file.startsWith(it) }) return FileVisitResult.CONTINUE
                if (!followSymlinks && attrs.isSymbolicLink) return FileVisitResult.CONTINUE
                try {
                    val name = file.fileName?.toString() ?: return FileVisitResult.CONTINUE
                    val ext = name.substringAfterLast('.', "").lowercase()
                    val size = attrs.size()
                    val created = attrs.creationTime()?.toMillis()
                    val modified = attrs.lastModifiedTime()?.toMillis()
                    val hidden = isHidden(file)
                    collected.add(FileEntry(
                        path = file.toAbsolutePath().toString(),
                        name = name,
                        extension = ext,
                        size = size,
                        createdAt = created,
                        modifiedAt = modified,
                        isHidden = hidden
                    ))
                    scanned++

                } catch (e: IOException) {

                }
                return FileVisitResult.CONTINUE
            }
            override fun visitFileFailed(file: Path, exc: IOException): FileVisitResult = FileVisitResult.CONTINUE
        }
        try {
            if (recursive) {
                Files.walkFileTree(root, visitor)
            } else {
                Files.list(root).use { stream ->
                    stream.forEach { p ->
                        try {
                            val attrs = Files.readAttributes(p, BasicFileAttributes::class.java)
                            if (attrs.isDirectory) return@forEach
                            if (!includeHidden && isHidden(p)) return@forEach
                            val name = p.fileName.toString()
                            val ext = name.substringAfterLast('.', "").lowercase()
                            collected.add(FileEntry(
                                path = p.toAbsolutePath().toString(),
                                name = name,
                                extension = ext,
                                size = attrs.size(),
                                createdAt = attrs.creationTime().toMillis(),
                                modifiedAt = attrs.lastModifiedTime().toMillis(),
                                isHidden = isHidden(p)
                            ))
                            scanned++
                        } catch (_: IOException) {}
                    }
                }
            }
        } catch (_: IOException) {}

        emit(ScanProgress(scanned = scanned, currentPath = root.toString()))
    }.flowOn(Dispatchers.IO)


    suspend fun scanSync(
        root: Path,
        recursive: Boolean = true,
        includeHidden: Boolean = false,
        followSymlinks: Boolean = false,
        excluded: Set<Path> = emptySet()
    ): ScanResult {
        val collected = mutableListOf<FileEntry>()
        fun process(p: Path) {
            try {
                val attrs = Files.readAttributes(p, BasicFileAttributes::class.java)
                if (attrs.isDirectory) return@process
                if (!includeHidden && isHidden(p)) return@process
                if (excluded.any { p.startsWith(it) }) return@process
                if (!followSymlinks && attrs.isSymbolicLink) return@process
                val name = p.fileName?.toString() ?: return@process
                val ext = name.substringAfterLast('.', "").lowercase()
                collected.add(FileEntry(
                    path = p.toAbsolutePath().toString(),
                    name = name,
                    extension = ext,
                    size = attrs.size(),
                    createdAt = attrs.creationTime().toMillis(),
                    modifiedAt = attrs.lastModifiedTime().toMillis(),
                    isHidden = isHidden(p)
                ))
            } catch (_: IOException) {}
        }
        try {
            if (recursive) {
                Files.walk(root).use { stream ->
                    for (p in stream) {
                        kotlinx.coroutines.currentCoroutineContext().ensureActive()
                        process(p)
                    }
                }
            } else {
                Files.list(root).use { stream ->
                    for (p in stream) {
                        kotlinx.coroutines.currentCoroutineContext().ensureActive()
                        process(p)
                    }
                }
            }
        } catch (_: IOException) {}
        val total = collected.sumOf { it.size }
        return ScanResult(collected, total)
    }

    private fun isHidden(p: Path): Boolean {
        return try {
            Files.isHidden(p) || p.fileName?.toString()?.startsWith(".") == true
        } catch (_: IOException) {
            p.fileName?.toString()?.startsWith(".") == true
        }
    }
}

private fun FileTime.toMillis(): Long = this.toMillis()