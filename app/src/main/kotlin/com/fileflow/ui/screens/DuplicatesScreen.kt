package com.fileflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fileflow.data.fs.DuplicateDetector
import com.fileflow.domain.model.DuplicateGroup
import com.fileflow.presentation.AppStateHolder
import com.fileflow.ui.localization.t
import com.fileflow.ui.util.Format
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.jewel.ui.component.Checkbox
import org.jetbrains.jewel.ui.component.CircularProgressIndicator
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text as JewelText
import java.io.File

@Composable
fun DuplicatesScreen(holder: AppStateHolder) {
    val scope = rememberCoroutineScope()
    var scanning by remember { mutableStateOf(false) }
    var groups by remember { mutableStateOf<List<DuplicateGroup>>(emptyList()) }
    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }
    var confirmDelete by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF222328))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                DefaultButton(
                    onClick = {
                        val files = holder.scanResult?.files
                        if (files == null) { message = "Scan a folder first in Dashboard"; return@DefaultButton }
                        scanning = true
                        scope.launch {
                            try {
                                groups = withContext(Dispatchers.IO) { DuplicateDetector().find(files) }
                                message = "Found ${groups.size} duplicate groups"
                            } catch (e: Exception) { message = e.message ?: "error" }
                            finally { scanning = false }
                        }
                    },
                    enabled = !scanning
                ) {
                    JewelText(if (scanning) "Finding Duplicates..." else t("common.scan"), color = Color.White)
                }

                if (groups.isNotEmpty()) {
                    JewelText(
                        "Reclaimable Space: ${Format.size(groups.sumOf { it.reclaimable })}",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }

            if (scanning) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            }
        }

        if (message.isNotBlank()) {
            JewelText(message, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }


        if (groups.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(groups) { g ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF222328))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            JewelText("Hash: ${g.hash.take(12)}...", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                JewelText("Size: ${Format.size(g.size)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                JewelText("${g.copies} copies", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                            }
                        }

                        g.files.forEach { f ->
                            val isChecked = selected.contains(f.path)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isChecked) Color(0xFF2C2E35) else Color(0xFF1B1C20))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { c ->
                                        selected = if (c) selected + f.path else selected - f.path
                                    }
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    JewelText(f.name, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                    JewelText(f.path, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DefaultButton(
                    onClick = { confirmDelete = true },
                    enabled = selected.isNotEmpty()
                ) {
                    JewelText("${t("common.delete")} (${selected.size})", color = Color.White)
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { JewelText("Delete selected duplicates?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = { JewelText("Selected files will be deleted from your disk. This action cannot be undone.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                DefaultButton(onClick = {
                    var deleted = 0
                    selected.forEach { p ->
                        try { if (File(p).delete()) deleted++ } catch (_: Exception) {}
                    }
                    selected = emptySet()
                    confirmDelete = false
                    message = "Deleted $deleted files"
                }) { JewelText(t("common.delete"), color = Color.White) }
            },
            dismissButton = {
                OutlinedButton(onClick = { confirmDelete = false }) { JewelText(t("common.cancel"), color = MaterialTheme.colorScheme.onSurface) }
            }
        )
    }
}