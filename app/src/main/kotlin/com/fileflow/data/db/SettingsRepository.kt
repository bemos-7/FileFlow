package com.fileflow.data.db

import com.fileflow.domain.model.Settings
import kotlinx.serialization.json.Json
import java.sql.PreparedStatement

class SettingsRepository {
    private val json = Json { ignoreUnknownKeys = true }
    fun load(): Settings {
        val c = Database.connection()
        val rs = c.createStatement().executeQuery("SELECT value FROM settings WHERE key='settings'")
        val v = if (rs.next()) rs.getString(1) else null
        rs.close()
        return v?.let { runCatching { json.decodeFromString<Settings>(it) }.getOrNull() } ?: Settings()
    }
    fun save(s: Settings) {
        val c = Database.connection()
        val ps = c.prepareStatement("INSERT INTO settings(key,value) VALUES('settings',?) ON CONFLICT(key) DO UPDATE SET value=excluded.value")

        ps.setString(1, json.encodeToString(Settings.serializer(), s))
        try { ps.executeUpdate() } catch (_: Exception) {
            val del = c.prepareStatement("DELETE FROM settings WHERE key='settings'"); del.executeUpdate(); del.close()
            val ins = c.prepareStatement("INSERT INTO settings(key,value) VALUES('settings',?)")
            ins.setString(1, json.encodeToString(Settings.serializer(), s)); ins.executeUpdate(); ins.close()
        }
        ps.close()
    }
}