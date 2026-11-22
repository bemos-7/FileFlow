package com.fileflow.data.fs

import com.fileflow.domain.model.DuplicateGroup
import com.fileflow.domain.model.FileEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest

class DuplicateDetector(private val parallelism: Int = 4) {
    suspend fun find(files: List<FileEntry>): List<DuplicateGroup> = coroutineScope {

        val bySize = files.filter { it.size > 0 }.groupBy { it.size }
        val candidates = bySize.filter { it.value.size > 1 }
        val sem = Semaphore(parallelism)
        val groups = mutableListOf<DuplicateGroup>()
        for ((size, list) in candidates) {

            val hashed = list.map { entry ->
                async(Dispatchers.IO) {
                    sem.withPermit {
                        val hash = sha256(entry.path)
                        entry to hash
                    }
                }
            }.awaitAll()
            val byHash = hashed.groupBy { it.second }
            for ((hash, pairs) in byHash) {
                if (pairs.size > 1) {
                    groups.add(DuplicateGroup(hash, size, pairs.map { it.first }))
                }
            }
        }
        groups.sortedByDescending { it.reclaimable }
    }

    private fun sha256(path: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        Files.newInputStream(Paths.get(path)).use { input ->
            val buf = ByteArray(8192)
            var r: Int
            while (true) {
                r = input.read(buf)
                if (r <= 0) break
                digest.update(buf, 0, r)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}