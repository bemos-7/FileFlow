package com.fileflow.data.db

import java.io.File
import java.sql.Connection
import java.sql.DriverManager

object Database {
    private var conn: Connection? = null
    fun init(dbFile: File) {
        if (conn != null) return
        dbFile.parentFile?.mkdirs()
        conn = DriverManager.getConnection("jdbc:sqlite:${dbFile.absolutePath}")
        conn!!.createStatement().use { st ->
            st.execute("""CREATE TABLE IF NOT EXISTS history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                type TEXT, timestamp INTEGER, source_dir TEXT, target_dir TEXT,
                count INTEGER, status TEXT
            )""")
            st.execute("""CREATE TABLE IF NOT EXISTS moves (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                history_id INTEGER, source_path TEXT, target_path TEXT
            )""")
            st.execute("""CREATE TABLE IF NOT EXISTS rules (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT, enabled INTEGER, priority INTEGER,
                extension TEXT, name_contains TEXT, older_than_days INTEGER,
                target_subdir TEXT, category_override TEXT
            )""")
            st.execute("""CREATE TABLE IF NOT EXISTS settings (
                key TEXT PRIMARY KEY, value TEXT
            )""")
        }
    }
    fun connection(): Connection = conn ?: throw IllegalStateException("DB not init")
}