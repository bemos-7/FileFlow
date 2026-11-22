package com.fileflow.ui.util

import java.text.DecimalFormat

object Format {
    fun size(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val units = arrayOf("KB","MB","GB","TB")
        var v = bytes.toDouble()
        var i = -1
        while (v >= 1024 && i < units.size - 1) { v /= 1024; i++ }
        return DecimalFormat("#,##0.0").format(v) + " " + units[i]
    }
}