package com.fileflow.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fileflow.domain.model.Settings
import com.fileflow.presentation.AppStateHolder
import com.fileflow.ui.localization.Language
import com.fileflow.ui.localization.t
import com.fileflow.ui.util.FileDialogs
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.GroupHeader
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.TextField

@Composable
fun SettingsScreen(holder: AppStateHolder, onThemeChange: (Boolean) -> Unit, onLanguageChange: (Language) -> Unit) {
    var s by remember { mutableStateOf(holder.settings) }

    fun save() {
        holder.saveSettings(s)
        onThemeChange(s.darkTheme)
        onLanguageChange(if (s.language == "ru") Language.RU else Language.EN)
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GroupHeader("Theme")
        Text("Dark theme (only)")

        GroupHeader("Language")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (s.language == "ru") DefaultButton(onClick = {}) { Text("RU") } else OutlinedButton(onClick = { s = s.copy(language = "ru"); save() }) { Text("RU") }
            if (s.language == "en") DefaultButton(onClick = {}) { Text("EN") } else OutlinedButton(onClick = { s = s.copy(language = "en"); save() }) { Text("EN") }
        }

        GroupHeader("Default target dir")
        Text(s.defaultTargetDir.ifBlank { "—" })
        DefaultButton(onClick = {
            FileDialogs.chooseDirectory()?.let { s = s.copy(defaultTargetDir = it.path); save() }
        }) { Text(t("common.browse")) }

        GroupHeader("Min large file (MB)")
        TextField(value = s.minLargeFileSizeMb.toString(), onValueChange = { s = s.copy(minLargeFileSizeMb = it.toLongOrNull() ?: s.minLargeFileSizeMb) }, modifier = Modifier.width(160.dp))
        DefaultButton(onClick = { save() }) { Text(t("common.save")) }

        GroupHeader("Conflict resolution")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("rename","skip").forEach { opt ->
                if (s.conflictResolution == opt) DefaultButton(onClick = {}) { Text(opt) }
                else OutlinedButton(onClick = { s = s.copy(conflictResolution = opt); save() }) { Text(opt) }
            }
        }

        GroupHeader("Scan options")
        Row(verticalAlignment = Alignment.CenterVertically) {
            org.jetbrains.jewel.ui.component.Checkbox(checked = s.scanHidden, onCheckedChange = { s = s.copy(scanHidden = it); save() })
            Text("Scan hidden files")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            org.jetbrains.jewel.ui.component.Checkbox(checked = s.followSymlinks, onCheckedChange = { s = s.copy(followSymlinks = it); save() })
            Text("Follow symlinks")
        }
    }
}