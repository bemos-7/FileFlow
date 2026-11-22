package com.fileflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fileflow.data.fs.FileScanner
import com.fileflow.data.fs.Organizer
import com.fileflow.data.fs.StructureMode
import com.fileflow.domain.model.FileEntry
import com.fileflow.domain.model.MovePlanItem
import com.fileflow.presentation.AppStateHolder
import com.fileflow.ui.localization.t
import com.fileflow.ui.util.FileDialogs
import com.fileflow.ui.util.Format
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.jewel.ui.component.Checkbox
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.TextField
import org.jetbrains.jewel.ui.component.Text as JewelText
import java.io.File

@Composable
fun OrganizerScreen(holder: AppStateHolder) {
    val scope = rememberCoroutineScope()
    var sourceDir by remember { mutableStateOf<File?>(null) }
    var recursive by remember { mutableStateOf(true) }
    var targetDir by remember { mutableStateOf<File?>(null) }
    var mode by remember { mutableStateOf(StructureMode.CATEGORY) }
    var files by remember { mutableStateOf<List<FileEntry>>(emptyList()) }
    var plan by remember { mutableStateOf<List<MovePlanItem>>(emptyList()) }
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var included by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var scanning by remember { mutableStateOf(false) }
    var dryRun by remember { mutableStateOf(false) }
    var executing by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    LaunchedEffect(plan) {
        included = plan.indices.toSet()
    }

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

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                JewelText(t("organizer.source") + ":", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(90.dp))
                JewelText(
                    sourceDir?.path ?: "Select folder...",
                    color = if (sourceDir != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    fontSize = 13.sp
                )
                OutlinedButton(onClick = { FileDialogs.chooseDirectory()?.let { sourceDir = it } }) {
                    JewelText(t("common.browse"), color = MaterialTheme.colorScheme.onSurface)
                }
            }


            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                JewelText(t("organizer.target") + ":", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(90.dp))
                JewelText(
                    targetDir?.path ?: (sourceDir?.path ?: "Same as source"),
                    color = if (targetDir != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    fontSize = 13.sp
                )
                OutlinedButton(onClick = { FileDialogs.chooseDirectory()?.let { targetDir = it } }) {
                    JewelText(t("common.browse"), color = MaterialTheme.colorScheme.onSurface)
                }
            }


            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                JewelText(t("organizer.structure") + ":", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(90.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StructureMode.entries.forEach { m ->
                        val selected = mode == m
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) MaterialTheme.colorScheme.primary else Color(0xFF2A2C32))
                                .border(1.dp, if (selected) MaterialTheme.colorScheme.primary else Color(0xFF383A42), RoundedCornerShape(8.dp))
                                .clickable { mode = m }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            JewelText(m.name, color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Checkbox(checked = recursive, onCheckedChange = { recursive = it })
                        JewelText(t("common.includeSubfolders"), color = MaterialTheme.colorScheme.onSurface)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Checkbox(checked = dryRun, onCheckedChange = { dryRun = it })
                        JewelText(t("common.dryrun"), color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                DefaultButton(
                    onClick = {
                        val sd = sourceDir ?: return@DefaultButton
                        val sys = listOf("/", "/System", "/Library", "/Applications", "C:\\Windows", "C:\\Program Files")
                        if (sys.any { sd.path == it || sd.path.startsWith("$it/") || sd.path.startsWith("$it\\") }) {
                            message = "Refusing to organize system folder"; return@DefaultButton
                        }
                        scanning = true
                        scope.launch {
                            try {
                                val res = withContext(Dispatchers.IO) {
                                    FileScanner().scanSync(sd.toPath(), recursive, holder.settings.scanHidden, holder.settings.followSymlinks, holder.settings.excludedDirs.map { File(it).toPath() }.toSet())
                                }
                                files = res.files
                                val td = targetDir ?: sd
                                plan = Organizer().plan(res.files, sd.toPath(), td.toPath(), mode, holder.rules)
                                message = "Planned ${plan.size} files"
                            } catch (e: Exception) { message = e.message ?: "error" }
                            finally { scanning = false }
                        }
                    },
                    enabled = !scanning && sourceDir != null
                ) {
                    JewelText(if (scanning) "Planning..." else t("common.scan"), color = Color.White)
                }
            }
        }

        if (message.isNotBlank()) {
            JewelText(message, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
        }


        if (plan.isNotEmpty()) {
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
                    JewelText("Planned Moves (${included.size} / ${plan.size} selected)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }

                LazyColumn(Modifier.weight(1f)) {
                    items(plan.size) { idx ->
                        val item = plan[idx]
                        val isChecked = included.contains(idx)

                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isChecked) Color(0xFF282A30) else Color.Transparent)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { c ->
                                    included = if (c) included + idx else included - idx
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                JewelText(item.sourcePath, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                JewelText(
                                    "→ ${item.targetPath}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable { editingIndex = idx }
                                )
                            }
                            JewelText(Format.size(item.size), modifier = Modifier.width(80.dp), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DefaultButton(
                        onClick = {
                            executing = true
                            scope.launch {
                                try {
                                    val selectedPlan = plan.filterIndexed { i, _ -> included.contains(i) }
                                    val org = Organizer()
                                    val executed = withContext(Dispatchers.IO) {
                                        org.executePlan(selectedPlan, dryRun) { _, _ -> }
                                    }
                                    if (!dryRun) {
                                        holder.historyRepo.add(com.fileflow.domain.model.OperationRecord(
                                            id = 0, type = "organize", timestamp = System.currentTimeMillis(),
                                            sourceDir = sourceDir!!.path, targetDir = (targetDir ?: sourceDir!!).path,
                                            count = executed.size, status = "done",
                                            moves = executed.map { com.fileflow.domain.model.MoveRecord(it.first, it.second) }
                                        ))
                                        holder.refreshHistory()
                                    }
                                    message = if (dryRun) "Dry run complete: ${selectedPlan.size} files" else "Moved ${executed.size} files"
                                } catch (e: Exception) { message = e.message ?: "error" }
                                finally { executing = false }
                            }
                        },
                        enabled = !executing && included.isNotEmpty()
                    ) {
                        JewelText(if (executing) "Moving..." else t("organizer.execute"), color = Color.White)
                    }

                    OutlinedButton(onClick = { plan = emptyList(); included = emptySet() }) {
                        JewelText(t("organizer.cancel"), color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }

    editingIndex?.let { idx ->
        var text by remember(idx) { mutableStateOf(plan[idx].targetPath) }
        AlertDialog(
            onDismissRequest = { editingIndex = null },
            title = { JewelText(t("common.editTarget"), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column {
                    JewelText("Target path:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    TextField(value = text, onValueChange = { text = it }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                DefaultButton(onClick = {
                    plan = plan.mapIndexed { i, it -> if (i == idx) it.copy(targetPath = text) else it }
                    editingIndex = null
                }) { JewelText(t("common.save"), color = Color.White) }
            },
            dismissButton = {
                OutlinedButton(onClick = { editingIndex = null }) { JewelText(t("common.cancel"), color = MaterialTheme.colorScheme.onSurface) }
            }
        )
    }
}