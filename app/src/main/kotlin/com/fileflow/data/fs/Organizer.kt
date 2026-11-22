package com.fileflow.data.fs

import com.fileflow.domain.CategoryClassifier
import com.fileflow.domain.model.Category
import com.fileflow.domain.model.FileEntry
import com.fileflow.domain.model.MovePlanItem
import com.fileflow.domain.model.SortRule
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class StructureMode { CATEGORY, CATEGORY_YEAR, CATEGORY_MONTH, CUSTOM_RULES }

class Organizer {
    fun plan(
        files: List<FileEntry>,
        sourceDir: Path,
        targetDir: Path,
        mode: StructureMode,
        rules: List<SortRule> = emptyList()
    ): List<MovePlanItem> {
        val enabledRules = rules.filter { it.enabled }.sortedBy { it.priority }
        val result = mutableListOf<MovePlanItem>()
        for (f in files) {

            val category = CategoryClassifier.classify(f.extension)
            val subdir = resolveSubdir(f, category, mode, enabledRules) ?: category.key
            val targetPath = targetDir.resolve(subdir).resolve(f.name)
            val safeTarget = uniqueTargetPath(targetPath)
            result.add(MovePlanItem(
                sourcePath = f.path,
                targetPath = safeTarget.toString(),
                category = category.key,
                size = f.size
            ))
        }
        return result
    }

    private fun resolveSubdir(f: FileEntry, category: Category, mode: StructureMode, rules: List<SortRule>): String? {

        for (rule in rules) {
            if (matches(f, rule)) {
                val base = rule.categoryOverride?.key ?: category.key
                return if (rule.targetSubdir.isNotBlank()) "${rule.targetSubdir.trim('/')}" else base
            }
        }
        if (mode == StructureMode.CUSTOM_RULES) {
            return category.key
        }
        val base = category.key
        val date = f.modifiedAt ?: f.createdAt
        return when (mode) {
            StructureMode.CATEGORY -> base
            StructureMode.CATEGORY_YEAR -> {
                val year = date?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).year } ?: "Unknown"
                "$base/$year"
            }
            StructureMode.CATEGORY_MONTH -> {
                val z = date?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()) }
                val year = z?.year ?: "Unknown"
                val month = z?.let { "%02d".format(it.monthValue) } ?: "00"
                "$base/$year/$month"
            }
            StructureMode.CUSTOM_RULES -> base
        }
    }

    private fun matches(f: FileEntry, rule: SortRule): Boolean {
        rule.extension?.let { if (!f.extension.equals(it.trimStart('.'), ignoreCase = true)) return false }
        rule.nameContains?.let { if (!f.name.contains(it, ignoreCase = true)) return false }
        rule.olderThanDays?.let {
            val cutoff = System.currentTimeMillis() - it.toLong() * 24 * 3600 * 1000
            val m = f.modifiedAt ?: f.createdAt ?: return false
            if (m > cutoff) return false
        }
        return true
    }

    fun uniqueTargetPath(target: Path): Path {
        if (!Files.exists(target)) return target
        val parent = target.parent
        val name = target.fileName.toString()
        val dot = name.lastIndexOf('.')
        val base = if (dot > 0) name.substring(0, dot) else name
        val ext = if (dot > 0) name.substring(dot) else ""
        var i = 1
        while (true) {
            val cand = parent.resolve("$base ($i)$ext")
            if (!Files.exists(cand)) return cand
            i++
        }
    }

    fun executePlan(
        plan: List<MovePlanItem>,
        dryRun: Boolean = false,
        onProgress: (Int, Int) -> Unit = { _, _ -> }
    ): List<Pair<String, String>> {
        val executed = mutableListOf<Pair<String, String>>()
        val total = plan.size
        plan.forEachIndexed { idx, item ->
            onProgress(idx + 1, total)
            if (dryRun) return@forEachIndexed
            val src = Paths.get(item.sourcePath)
            val dst = Paths.get(item.targetPath)
            try {
                Files.createDirectories(dst.parent)
                if (Files.exists(dst)) {

                    return@forEachIndexed
                }

                try {
                    Files.move(src, dst, java.nio.file.StandardCopyOption.ATOMIC_MOVE)
                } catch (e: Exception) {

                    Files.copy(src, dst, java.nio.file.StandardCopyOption.COPY_ATTRIBUTES)
                    if (Files.size(src) == Files.size(dst)) {
                        Files.delete(src)
                    } else {
                        Files.deleteIfExists(dst)
                        throw e
                    }
                }
                executed.add(src.toString() to dst.toString())
            } catch (_: Exception) {

            }
        }
        return executed
    }
}