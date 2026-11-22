package com.fileflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
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
import com.fileflow.presentation.AppStateHolder
import com.fileflow.ui.localization.t
import kotlinx.coroutines.launch
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text as JewelText
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun HistoryScreen(holder: AppStateHolder) {
    val scope = rememberCoroutineScope()
    var history by remember { mutableStateOf(holder.historyRepo.list()) }
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
            JewelText("Operation History (${history.size})", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DefaultButton(
                    onClick = {
                        scope.launch {
                            message = holder.historyRepo.undoLast()
                            history = holder.historyRepo.list()
                            holder.refreshHistory()
                        }
                    }
                ) {
                    JewelText(t("common.undoLast"), color = Color.White)
                }

                OutlinedButton(onClick = { history = holder.historyRepo.list() }) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onSurface)
                        JewelText(t("common.refresh"), color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        if (message.isNotBlank()) {
            JewelText(message, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
        }


        if (history.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(history) { h ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF222328))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                JewelText(h.type.replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (h.status == "done") Color(0xFF1E3A2B) else Color(0xFF382222))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    JewelText(h.status, color = if (h.status == "done") Color(0xFF34D399) else Color(0xFFF87171), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                            }

                            JewelText(
                                SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(h.timestamp)),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(Modifier.height(4.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(Modifier.weight(1f)) {
                                JewelText("Source:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                JewelText(h.sourceDir, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            }

                            Column(Modifier.weight(1f)) {
                                JewelText("Target:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                JewelText(h.targetDir, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                JewelText("Files Processed:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                JewelText("${h.count}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}