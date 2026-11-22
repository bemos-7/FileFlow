package com.fileflow.data.db

import com.fileflow.domain.model.Category
import com.fileflow.domain.model.SortRule
import java.sql.Statement

class RulesRepository {
    fun list(): List<SortRule> {
        val rs = Database.connection().createStatement().executeQuery("SELECT * FROM rules ORDER BY priority ASC")
        val out = mutableListOf<SortRule>()
        while (rs.next()) {
            out.add(SortRule(
                id = rs.getLong("id"),
                name = rs.getString("name"),
                enabled = rs.getInt("enabled") == 1,
                priority = rs.getInt("priority"),
                extension = rs.getString("extension"),
                nameContains = rs.getString("name_contains"),
                olderThanDays = rs.getObject("older_than_days") as? Int,
                targetSubdir = rs.getString("target_subdir") ?: "",
                categoryOverride = rs.getString("category_override")?.let { runCatching { Category.valueOf(it) }.getOrNull() }
            ))
        }
        rs.close()
        return out
    }
    fun save(rule: SortRule): Long {
        val c = Database.connection()
        return if (rule.id > 0) {
            val ps = c.prepareStatement("UPDATE rules SET name=?,enabled=?,priority=?,extension=?,name_contains=?,older_than_days=?,target_subdir=?,category_override=? WHERE id=?")
            ps.setString(1, rule.name); ps.setInt(2, if (rule.enabled) 1 else 0); ps.setInt(3, rule.priority)
            ps.setString(4, rule.extension); ps.setString(5, rule.nameContains)
            if (rule.olderThanDays == null) ps.setNull(6, java.sql.Types.INTEGER) else ps.setInt(6, rule.olderThanDays)
            ps.setString(7, rule.targetSubdir); ps.setString(8, rule.categoryOverride?.name)
            ps.setLong(9, rule.id); ps.executeUpdate(); ps.close(); rule.id
        } else {
            val ps = c.prepareStatement("INSERT INTO rules(name,enabled,priority,extension,name_contains,older_than_days,target_subdir,category_override) VALUES(?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)
            ps.setString(1, rule.name); ps.setInt(2, if (rule.enabled) 1 else 0); ps.setInt(3, rule.priority)
            ps.setString(4, rule.extension); ps.setString(5, rule.nameContains)
            if (rule.olderThanDays == null) ps.setNull(6, java.sql.Types.INTEGER) else ps.setInt(6, rule.olderThanDays)
            ps.setString(7, rule.targetSubdir); ps.setString(8, rule.categoryOverride?.name)
            ps.executeUpdate()
            val rs = ps.generatedKeys; val id = if (rs.next()) rs.getLong(1) else -1; rs.close(); ps.close(); id
        }
    }
    fun delete(id: Long) {
        val ps = Database.connection().prepareStatement("DELETE FROM rules WHERE id=?")
        ps.setLong(1, id); ps.executeUpdate(); ps.close()
    }
}