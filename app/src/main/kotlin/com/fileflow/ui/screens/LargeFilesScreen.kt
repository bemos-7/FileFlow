package com.fileflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fileflow.data.fs.FileScanner
import com.fileflow.domain.model.FileEntry
import com.fileflow.presentation.AppStateHolder
import com.fileflow.ui.localization.t
import com.fileflow.ui.util.FileDialogs
import com.fileflow.ui.util.Format
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.jewel.ui.component.CircularProgressIndicator
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.TextField
import org.jetbrains.jewel.ui.component.Text as JewelText
import java.io.File

@Composable
fun LargeFilesScreen(holder: AppStateHolder) {
    val scope = rememberCoroutineScope()
    var minMb by remember { mutableStateOf(holder.settings.minLargeFileSizeMb) }
    var minMbText by remember { mutableStateOf(holder.settings.minLargeFileSizeMb.toString()) }
    var query by remember { mutableStateOf("") }
    var files by remember { mutableStateOf<List<FileEntry>>(emptyList()) }
    var scanning by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    val filtered = files
        .filter { it.size >= minMb * 1024 * 1024 }
        .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
        .sortedByDescending { it.size }

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF222328))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DefaultButton(
                    onClick = {
                        val dir = FileDialogs.chooseDirectory()
                        if (dir != null) {
                            scanning = true
                            scope.launch {
                                try {
                                    val res = withContext(Dispatchers.IO) {
                                        FileScanner().scanSync(
                                            dir.toPath(),
                                            true,
                                            holder.settings.scanHidden,
                                            holder.settings.followSymlinks,
                                            holder.settings.excludedDirs.map { File(it).toPath() }.toSet()
                                        )
                                    }
                                    files = res.files
                                    message = "Scanned ${res.files.size} files"
                                } catch (e: Exception) { message = e.message ?: "error" }
                                finally { scanning = false }
                            }
                        }
                    },
                    enabled = !scanning
                ) {
                    JewelText(if (scanning) "Scanning..." else t("common.scan"), color = Color.White)
                }

                if (scanning) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                }

                Spacer(Modifier.weight(1f))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    JewelText("${t("common.search")}:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.width(180.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    JewelText("${t("common.minMb")}:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextField(
                        value = minMbText,
                        onValueChange = {
                            minMbText = it
                            minMb = it.toLongOrNull() ?: minMb
                        },
                        modifier = Modifier.width(90.dp)
                    )
                }
            }
        }

        if (message.isNotBlank()) {
            JewelText(message, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }


        if (filtered.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF222328))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    JewelText("Found ${filtered.size} large files", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    JewelText("Total: ${Format.size(filtered.sumOf { it.size })}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filtered) { f ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1B1C20))
                                .border(1.dp, Color(0xFF2B2D33), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                JewelText(f.name, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                JewelText(f.path, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            JewelText(Format.size(f.size), modifier = Modifier.width(90.dp), fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            OutlinedButton(
                                onClick = {
                                    try {
                                        val os = System.getProperty("os.name").lowercase()
                                        when {
                                            os.contains("mac") -> Runtime.getRuntime().exec(arrayOf("open", "-R", f.path))
                                            os.contains("win") -> Runtime.getRuntime().exec(arrayOf("explorer", "/select,", f.path))
                                            else -> Runtime.getRuntime().exec(arrayOf("xdg-open", File(f.path).parent))
                                        }
                                    } catch (_: Exception) {}
                                }
                            ) {
                                JewelText(t("common.reveal"), color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}