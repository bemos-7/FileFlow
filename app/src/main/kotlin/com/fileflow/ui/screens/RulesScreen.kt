package com.fileflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fileflow.domain.model.SortRule
import com.fileflow.presentation.AppStateHolder
import com.fileflow.ui.localization.t
import org.jetbrains.jewel.ui.component.Checkbox
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.TextField
import org.jetbrains.jewel.ui.component.Text as JewelText

@Composable
fun RulesScreen(holder: AppStateHolder) {
    var rules by remember { mutableStateOf(holder.rules) }
    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<SortRule?>(null) }

    fun refresh() { rules = holder.rulesRepo.list(); holder.rules = rules }

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
            JewelText("Custom Sort Rules (${rules.size})", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

            DefaultButton(onClick = { editing = null; showForm = true }) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    JewelText(t("common.addRule"), color = Color.White)
                }
            }
        }


        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(rules) { r ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF222328))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = r.enabled,
                        onCheckedChange = { c ->
                            holder.rulesRepo.save(r.copy(enabled = c)); refresh()
                        }
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            JewelText(r.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                JewelText("Priority ${r.priority}", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        JewelText(
                            "ext: ${r.extension ?: "*"}  |  contains: ${r.nameContains ?: "-"}  |  olderThan: ${r.olderThanDays ?: "-"} days  →  ${r.targetSubdir}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF2C2E35))
                                .clickable { editing = r; showForm = true }
                                .padding(8.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF382222))
                                .clickable { holder.rulesRepo.delete(r.id); refresh() }
                                .padding(8.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        RuleFormDialog(
            initial = editing,
            onDismiss = { showForm = false },
            onSave = { rule ->
                holder.rulesRepo.save(rule)
                refresh()
                showForm = false
            }
        )
    }
}

@Composable
fun RuleFormDialog(initial: SortRule?, onDismiss: () -> Unit, onSave: (SortRule) -> Unit) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var ext by remember { mutableStateOf(initial?.extension ?: "") }
    var contains by remember { mutableStateOf(initial?.nameContains ?: "") }
    var older by remember { mutableStateOf(initial?.olderThanDays?.toString() ?: "") }
    var target by remember { mutableStateOf(initial?.targetSubdir ?: "") }
    var prio by remember { mutableStateOf(initial?.priority?.toString() ?: "10") }
    var enabled by remember { mutableStateOf(initial?.enabled ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { JewelText(if (initial == null) "New Sort Rule" else "Edit Sort Rule", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FormField("Rule Name", name) { name = it }
                FormField("File Extension (e.g. pdf)", ext) { ext = it }
                FormField("Filename Contains", contains) { contains = it }
                FormField("Older Than (Days)", older) { older = it }
                FormField("Target Subdirectory", target) { target = it }
                FormField("Priority", prio) { prio = it }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Checkbox(checked = enabled, onCheckedChange = { enabled = it })
                    JewelText("Enabled", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        },
        confirmButton = {
            DefaultButton(onClick = {
                onSave(SortRule(
                    id = initial?.id ?: 0,
                    name = name,
                    enabled = enabled,
                    priority = prio.toIntOrNull() ?: 10,
                    extension = ext.takeIf { it.isNotBlank() },
                    nameContains = contains.takeIf { it.isNotBlank() },
                    olderThanDays = older.toIntOrNull(),
                    targetSubdir = target,
                    categoryOverride = null
                ))
            }) { JewelText(t("common.save"), color = Color.White) }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { JewelText(t("common.cancel"), color = MaterialTheme.colorScheme.onSurface) }
        }
    )
}

@Composable
fun FormField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column {
        JewelText(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        TextField(value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth())
    }
}