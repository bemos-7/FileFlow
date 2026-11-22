package com.fileflow.ui.util

import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileSystemView

object FileDialogs {
    fun chooseDirectory(): File? {
        val chooser = JFileChooser(FileSystemView.getFileSystemView().homeDirectory)
        chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        chooser.dialogTitle = "Select folder"
        val result = chooser.showOpenDialog(null)
        return if (result == JFileChooser.APPROVE_OPTION) chooser.selectedFile else null
    }
}