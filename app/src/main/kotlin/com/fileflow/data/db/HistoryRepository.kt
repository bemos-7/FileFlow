package com.fileflow.data.db

import com.fileflow.domain.model.MoveRecord
import com.fileflow.domain.model.OperationRecord
import java.sql.Statement

class HistoryRepository {
    fun add(record: OperationRecord): Long {
        val c = Database.connection()
        val ps = c.prepareStatement(
            "INSERT INTO history(type,timestamp,source_dir,target_dir,count,status) VALUES(?,?,?,?,?,?)",
            Statement.RETURN_GENERATED_KEYS
        )
        ps.setString(1, record.type)
        ps.setLong(2, record.timestamp)
        ps.setString(3, record.sourceDir)
        ps.setString(4, record.targetDir)
        ps.setInt(5, record.count)
        ps.setString(6, record.status)
        ps.executeUpdate()
        val rs = ps.generatedKeys
        val id = if (rs.next()) rs.getLong(1) else -1
        rs.close(); ps.close()
        val mps = c.prepareStatement("INSERT INTO moves(history_id,source_path,target_path) VALUES(?,?,?)")
        for (m in record.moves) {
            mps.setLong(1, id)
            mps.setString(2, m.sourcePath)
            mps.setString(3, m.targetPath)
            mps.executeUpdate()
        }
        mps.close()
        return id
    }

    fun list(): List<OperationRecord> {
        val c = Database.connection()
        val rs = c.createStatement().executeQuery("SELECT * FROM history ORDER BY timestamp DESC")
        val out = mutableListOf<OperationRecord>()
        while (rs.next()) {
            val id = rs.getLong("id")
            val moves = mutableListOf<MoveRecord>()
            val mrs = c.createStatement().executeQuery("SELECT source_path,target_path FROM moves WHERE history_id=$id")
            while (mrs.next()) moves.add(MoveRecord(mrs.getString(1), mrs.getString(2)))
            mrs.close()
            out.add(OperationRecord(
                id = id,
                type = rs.getString("type"),
                timestamp = rs.getLong("timestamp"),
                sourceDir = rs.getString("source_dir"),
                targetDir = rs.getString("target_dir"),
                count = rs.getInt("count"),
                status = rs.getString("status"),
                moves = moves
            ))
        }
        rs.close()
        return out
    }

    fun last(): OperationRecord? = list().firstOrNull()

    fun undoLast(): String {
        val rec = last() ?: return "No history"

        val c = Database.connection()
        var restored = 0
        var skipped = 0
        val cleanedDirs = mutableSetOf<java.nio.file.Path>()
        for (m in rec.moves) {
            val src = java.nio.file.Paths.get(m.targetPath)
            val dst = java.nio.file.Paths.get(m.sourcePath)
            if (!java.nio.file.Files.exists(src)) { skipped++; continue }
            if (java.nio.file.Files.exists(dst)) { skipped++; continue }
            try {
                java.nio.file.Files.createDirectories(dst.parent)
                java.nio.file.Files.move(src, dst)
                restored++
                src.parent?.let { cleanedDirs.add(it) }
            } catch (_: Exception) { skipped++ }
        }

        val targetRoot = java.nio.file.Paths.get(rec.targetDir)
        for (dir in cleanedDirs) {
            var cur: java.nio.file.Path? = dir
            while (cur != null && cur != targetRoot && cur.startsWith(targetRoot)) {
                if (java.nio.file.Files.isDirectory(cur)) {
                    val empty = java.nio.file.Files.list(cur).use { it.count() == 0L }
                    if (empty) {
                        try { java.nio.file.Files.delete(cur) } catch (_: Exception) { break }
                        cur = cur.parent
                    } else break
                } else break
            }
        }

        val ps = c.prepareStatement("UPDATE history SET status=? WHERE id=?")
        ps.setString(1, "undone")
        ps.setLong(2, rec.id)
        ps.executeUpdate(); ps.close()
        return "Restored $restored, skipped $skipped"
    }
}