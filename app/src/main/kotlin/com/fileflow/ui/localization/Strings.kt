package com.fileflow.ui.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember

enum class Language { RU, EN }

val LocalStrings = compositionLocalOf<Strings> { Strings(Language.EN) }

class Strings(val language: Language) {
    private val ru = mapOf(
        "app.title" to "FileFlow",
        "nav.dashboard" to "Панель",
        "nav.organizer" to "Организатор",
        "nav.duplicates" to "Дубликаты",
        "nav.largefiles" to "Большие файлы",
        "nav.rules" to "Правила",
        "nav.history" to "История",
        "nav.settings" to "Настройки",
        "dashboard.title" to "Панель",
        "dashboard.scanned" to "Просканировано файлов",
        "dashboard.totalSize" to "Общий объём",
        "dashboard.duplicates" to "Дубликаты",
        "dashboard.categories" to "Категории",
        "dashboard.recent" to "Последние операции",
        "dashboard.scan" to "Сканировать",
        "organizer.title" to "Организатор",
        "organizer.source" to "Исходная папка",
        "organizer.target" to "Целевая папка",
        "organizer.structure" to "Структура",
        "organizer.preview" to "Предпросмотр",
        "organizer.execute" to "Выполнить",
        "organizer.cancel" to "Отмена",
        "duplicates.title" to "Дубликаты",
        "largefiles.title" to "Большие файлы",
        "rules.title" to "Правила",
        "history.title" to "История",
        "settings.title" to "Настройки",
        "settings.language" to "Язык",
        "settings.theme" to "Тема",
        "settings.dark" to "Тёмная",
        "settings.light" to "Светлая",
        "common.browse" to "Обзор",
        "common.scan" to "Сканировать",
        "common.cancel" to "Отмена",
        "common.confirm" to "Подтвердить",
        "common.delete" to "Удалить",
        "common.save" to "Сохранить",
        "common.addRule" to "Добавить правило",
        "common.undoLast" to "Отменить последнюю",
        "common.refresh" to "Обновить",
        "common.editTarget" to "Изменить путь",
        "common.includeSubfolders" to "Включая подпапки",
        "common.search" to "Поиск",
        "common.reveal" to "Показать",
        "common.minMb" to "Мин. МБ",
        "common.dryrun" to "Сухой запуск",
        "dashboard.bycategory" to "По категориям",
        "duplicates.reclaimable" to "Можно освободить",
    )
    private val en = mapOf(
        "app.title" to "FileFlow",
        "nav.dashboard" to "Dashboard",
        "nav.organizer" to "Organizer",
        "nav.duplicates" to "Duplicates",
        "nav.largefiles" to "Large Files",
        "nav.rules" to "Rules",
        "nav.history" to "History",
        "nav.settings" to "Settings",
        "dashboard.title" to "Dashboard",
        "dashboard.scanned" to "Scanned files",
        "dashboard.totalSize" to "Total size",
        "dashboard.duplicates" to "Duplicates",
        "dashboard.categories" to "Categories",
        "dashboard.recent" to "Recent operations",
        "dashboard.scan" to "Scan",
        "organizer.title" to "Organizer",
        "organizer.source" to "Source folder",
        "organizer.target" to "Target folder",
        "organizer.structure" to "Structure",
        "organizer.preview" to "Preview",
        "organizer.execute" to "Execute",
        "organizer.cancel" to "Cancel",
        "duplicates.title" to "Duplicates",
        "largefiles.title" to "Large Files",
        "rules.title" to "Rules",
        "history.title" to "History",
        "settings.title" to "Settings",
        "settings.language" to "Language",
        "settings.theme" to "Theme",
        "settings.dark" to "Dark",
        "settings.light" to "Light",
        "common.browse" to "Browse",
        "common.scan" to "Scan",
        "common.cancel" to "Cancel",
        "common.confirm" to "Confirm",
        "common.delete" to "Delete",
        "common.save" to "Save",
        "common.addRule" to "Add rule",
        "common.undoLast" to "Undo last",
        "common.refresh" to "Refresh",
        "common.editTarget" to "Edit target",
        "common.includeSubfolders" to "Include subfolders",
        "common.search" to "Search",
        "common.reveal" to "Reveal",
        "common.minMb" to "Min MB",
        "common.dryrun" to "Dry run",
        "dashboard.bycategory" to "By category",
        "duplicates.reclaimable" to "Reclaimable",
    )

    operator fun get(key: String): String {
        val m = if (language == Language.RU) ru else en
        return m[key] ?: key
    }
}

@Composable
fun ProvideStrings(language: Language, content: @Composable () -> Unit) {
    val strings = remember(language) { Strings(language) }
    CompositionLocalProvider(LocalStrings provides strings, content = content)
}

@Composable
fun t(key: String): String = LocalStrings.current[key]