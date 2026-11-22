package com.fileflow

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fileflow.presentation.AppStateHolder
import com.fileflow.ui.localization.Language
import com.fileflow.ui.localization.ProvideStrings
import com.fileflow.ui.localization.t
import com.fileflow.ui.navigation.Screen
import com.fileflow.ui.screens.*
import com.fileflow.ui.theme.FileFlowTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme
import org.jetbrains.jewel.ui.component.Divider
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text as JewelText

@Composable
fun FileFlowApp(holder: AppStateHolder) {
    var darkTheme by remember { mutableStateOf(holder.settings.darkTheme) }
    var language by remember { mutableStateOf(holder.languageEnum()) }
    var screen by remember { mutableStateOf(Screen.Dashboard) }
    val hazeState = remember { HazeState() }
    var undoMessage by remember { mutableStateOf("") }

    LaunchedEffect(holder.settings) {
        darkTheme = holder.settings.darkTheme
        language = holder.languageEnum()
    }

    IntUiTheme(true, false) {
        FileFlowTheme {
            ProvideStrings(language) {
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
                    Row(
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .haze(hazeState)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        Sidebar(screen = screen, onScreen = { screen = it })


                        Column(
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                        ) {
                            TopBar(
                                title = localizedScreenName(screen),
                                onUndo = {
                                    undoMessage = holder.historyRepo.undoLast()
                                    holder.refreshHistory()
                                }
                            )

                            if (undoMessage.isNotBlank()) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(8.dp))
                                        JewelText(undoMessage, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }

                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .padding(20.dp)
                            ) {
                                when (screen) {
                                    Screen.Dashboard -> DashboardScreen(holder)
                                    Screen.Organizer -> OrganizerScreen(holder)
                                    Screen.Duplicates -> DuplicatesScreen(holder)
                                    Screen.LargeFiles -> LargeFilesScreen(holder)
                                    Screen.Rules -> RulesScreen(holder)
                                    Screen.History -> HistoryScreen(holder)
                                    Screen.Settings -> SettingsScreen(holder, onThemeChange = { darkTheme = it }, onLanguageChange = { language = it })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun localizedScreenName(screen: Screen): String = when (screen) {
    Screen.Dashboard -> t("nav.dashboard")
    Screen.Organizer -> t("nav.organizer")
    Screen.Duplicates -> t("nav.duplicates")
    Screen.LargeFiles -> t("nav.largefiles")
    Screen.Rules -> t("nav.rules")
    Screen.History -> t("nav.history")
    Screen.Settings -> t("nav.settings")
}

@Composable
fun Sidebar(screen: Screen, onScreen: (Screen) -> Unit) {
    Column(
        Modifier
            .width(230.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF141518))
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Text("FileFlow", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        }


        val mainScreens = listOf(Screen.Dashboard, Screen.Organizer, Screen.Duplicates, Screen.LargeFiles)
        val toolScreens = listOf(Screen.Rules, Screen.History, Screen.Settings)

        mainScreens.forEach { s ->
            SidebarItem(screen = s, isSelected = s == screen, onClick = { onScreen(s) })
            Spacer(Modifier.height(4.dp))
        }

        Spacer(Modifier.height(20.dp))
        Divider(Modifier.fillMaxWidth().padding(vertical = 8.dp))
        Spacer(Modifier.height(8.dp))

        toolScreens.forEach { s ->
            SidebarItem(screen = s, isSelected = s == screen, onClick = { onScreen(s) })
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
fun SidebarItem(screen: Screen, isSelected: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val icon = when (screen) {
        Screen.Dashboard -> Icons.Default.Home
        Screen.Organizer -> Icons.Default.List
        Screen.Duplicates -> Icons.Default.Star
        Screen.LargeFiles -> Icons.Default.Info
        Screen.Rules -> Icons.Default.Build
        Screen.History -> Icons.Default.DateRange
        Screen.Settings -> Icons.Default.Settings
    }

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> Color(0xFF1E2A44)
            isHovered -> Color(0xFF1A1C22)
            else -> Color.Transparent
        },
        label = "sidebarBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF8AAEFF) else Color(0xFFAEB2BA),
        label = "sidebarContent"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .hoverable(interactionSource)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = contentColor)
        Spacer(Modifier.width(10.dp))
        org.jetbrains.jewel.ui.component.Text(
            text = localizedScreenName(screen),
            color = contentColor,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
fun TopBar(title: String, onUndo: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.weight(1f))
    }
}