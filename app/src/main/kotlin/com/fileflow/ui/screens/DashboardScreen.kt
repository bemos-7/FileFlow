package com.fileflow.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fileflow.data.fs.FileScanner
import com.fileflow.domain.CategoryClassifier
import com.fileflow.domain.model.FileEntry
import com.fileflow.presentation.AppStateHolder
import com.fileflow.ui.localization.t
import com.fileflow.ui.util.FileDialogs
import com.fileflow.ui.util.Format
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun DashboardScreen(holder: AppStateHolder) {
    val scope = rememberCoroutineScope()
    var scanning by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var recursive by remember { mutableStateOf(true) }

    val result = holder.scanResult
    val files = result?.files ?: emptyList()
    val totalSize = result?.totalSize ?: 0L

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            org.jetbrains.jewel.ui.component.Text("Dashboard", fontWeight = FontWeight.Bold, style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
            org.jetbrains.jewel.ui.component.Text("Overview of your files and recent activity", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
        }


        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            org.jetbrains.jewel.ui.component.DefaultButton(onClick = {
                val dir = FileDialogs.chooseDirectory()
                if (dir != null) {
                    scanning = true
                    error = null
                    scope.launch {
                        try {
                            val res = withContext(Dispatchers.IO) {
                                FileScanner().scanSync(dir.toPath(), recursive, holder.settings.scanHidden, holder.settings.followSymlinks, holder.settings.excludedDirs.map { File(it).toPath() }.toSet())
                            }
                            holder.scanResult = res
                            progress = "Scanned ${res.files.size} files"
                        } catch (e: Exception) {
                            error = e.message
                        } finally {
                            scanning = false
                        }
                    }
                }
            }, enabled = !scanning) {
                org.jetbrains.jewel.ui.component.Text(if (scanning) "..." else t("common.scan"))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                org.jetbrains.jewel.ui.component.Checkbox(checked = recursive, onCheckedChange = { recursive = it })
                org.jetbrains.jewel.ui.component.Text(t("common.includeSubfolders"))
            }
            if (scanning) androidx.compose.material3.CircularProgressIndicator(Modifier.size(24.dp))
        }
        if (progress.isNotBlank()) org.jetbrains.jewel.ui.component.Text(progress)
        error?.let { org.jetbrains.jewel.ui.component.Text(it) }

        if (files.isNotEmpty()) {

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Scanned files", files.size.toString())
                StatCard("Total size", Format.size(totalSize))
                StatCard("Categories", files.map { CategoryClassifier.classify(it.extension).key }.distinct().size.toString())
                StatCard("Duplicates", holder.scanResult?.let { "—" } ?: "—")
            }


            org.jetbrains.jewel.ui.component.Text("By category", fontWeight = FontWeight.SemiBold)
            val byCat = files.groupBy { CategoryClassifier.classify(it.extension).key }
            byCat.forEach { (cat, list) ->
                val size = list.sumOf { it.size }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    org.jetbrains.jewel.ui.component.Text(cat, modifier = Modifier.width(130.dp))
                    androidx.compose.material3.LinearProgressIndicator(
                        progress = { if (totalSize > 0) size.toFloat() / totalSize else 0f },
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                    )
                    org.jetbrains.jewel.ui.component.Text(Format.size(size), modifier = Modifier.width(80.dp))
                }
            }
        }


        if (holder.history.isNotEmpty()) {
            org.jetbrains.jewel.ui.component.Text("Recent operations", fontWeight = FontWeight.SemiBold)
            holder.history.take(5).forEach { h ->
                CardRow("${h.type} • ${h.count} files • ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(java.util.Date(h.timestamp))}")
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .width(180.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
            .border(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.outline, androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            org.jetbrains.jewel.ui.component.Text(label, style = androidx.compose.material3.MaterialTheme.typography.labelMedium)
            org.jetbrains.jewel.ui.component.Text(value, style = androidx.compose.material3.MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CardRow(text: String) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
            .border(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.outline, androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        org.jetbrains.jewel.ui.component.Text(text)
    }
}