package com.fileflow.domain

import com.fileflow.domain.model.Category

object CategoryClassifier {
    private val map: Map<String, Category> = buildMap {
        listOf("jpg","jpeg","png","webp","gif","bmp","svg","heic","tiff").forEach { put(it, Category.Images) }
        listOf("mp4","mkv","avi","mov","webm","flv").forEach { put(it, Category.Videos) }
        listOf("mp3","wav","flac","aac","ogg","m4a").forEach { put(it, Category.Audio) }
        listOf("pdf","doc","docx","txt","rtf","odt","xls","xlsx","ppt","pptx").forEach { put(it, Category.Documents) }
        listOf("zip","rar","7z","tar","gz","bz2").forEach { put(it, Category.Archives) }
        listOf("exe","msi","dmg","pkg","deb","rpm","apk").forEach { put(it, Category.Installers) }
        listOf("kt","kts","java","py","js","ts","html","css","json","xml","yaml","yml","c","cpp","cs","go","rs").forEach { put(it, Category.Development) }
        listOf("stl","obj","fbx","blend","glb","gltf","3mf","step","stp").forEach { put(it, Category.Models3D) }
        listOf("ttf","otf","woff","woff2").forEach { put(it, Category.Fonts) }
        listOf("torrent").forEach { put(it, Category.Torrents) }
    }

    fun classify(extension: String): Category =
        map[extension.lowercase().trimStart('.')] ?: Category.Other

    fun allExtensions(): Map<String, Category> = map
}