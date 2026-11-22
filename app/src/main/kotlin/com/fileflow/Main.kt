package com.fileflow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Text as JewelText
import com.fileflow.presentation.AppStateHolder
import java.awt.Frame
import java.io.File

@Composable
fun CustomTitleBar(window: java.awt.Frame) {
    println("INIT window loc=${window.x},${window.y} size=${window.width},${window.height}")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(Color(0xFF191A1D))
            .pointerInput(Unit) {
                var startWindow = window.locationOnScreen
                var startMouse = java.awt.MouseInfo.getPointerInfo().location
                detectDragGestures(
                    onDragStart = {
                        startWindow = window.locationOnScreen
                        startMouse = java.awt.MouseInfo.getPointerInfo().location
                    },
                    onDrag = { _, _ ->
                        val mouse = java.awt.MouseInfo.getPointerInfo().location
                        val nx = startWindow.x + (mouse.x - startMouse.x)
                        val ny = startWindow.y + (mouse.y - startMouse.y)
                        window.setLocation(nx, ny)
                    }
                )
            }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TitleBarButton(onClick = { window.dispose() }) { JewelText("×") }
            TitleBarButton(onClick = { window.state = Frame.ICONIFIED }) { JewelText("–") }
            TitleBarButton(onClick = {
                window.extendedState = if (window.extendedState == Frame.MAXIMIZED_BOTH) Frame.NORMAL else Frame.MAXIMIZED_BOTH
            }) { JewelText("□") }
        }
        Spacer(Modifier.width(12.dp))

        JewelText("FileFlow", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        Spacer(Modifier.weight(1f))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            androidx.compose.material3.Icon(
                painter = androidx.compose.ui.res.painterResource("github.svg"),
                contentDescription = "GitHub",
                modifier = Modifier.size(20.dp).clickable { java.awt.Desktop.getDesktop().browse(java.net.URI("https://github.com/bemos-7")) },
                tint = Color.White
            )
        }
    }
}

@Composable
fun TitleBarButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

fun main() {
    val dataDir = File(System.getProperty("user.home"), ".fileflow")
    dataDir.mkdirs()
    val holder = AppStateHolder(dataDir)
    holder.init()
    application {
        val iconBitmap = remember {
            try {
                val stream = Thread.currentThread().contextClassLoader.getResourceAsStream("app-icon.png")
                val img = javax.imageio.ImageIO.read(stream)
                img?.toComposeImageBitmap()
            } catch (e: Exception) { null }
        }
        val state = rememberWindowState(width = 1100.dp, height = 720.dp)
        Window(
            onCloseRequest = ::exitApplication,
            state = state,
            title = "FileFlow",
            icon = iconBitmap?.let { androidx.compose.ui.graphics.painter.BitmapPainter(it) },
            undecorated = true,
            transparent = true
        ) {
            window.minimumSize = java.awt.Dimension(900, 600)
            window.setBackground(java.awt.Color(0, 0, 0, 0))
            IntUiTheme(true, false) {
                Column(Modifier.fillMaxSize()) {
                    CustomTitleBar(window as java.awt.Frame)
                    FileFlowApp(holder)
                }
            }
        }
    }
}