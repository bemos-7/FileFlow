package com.fileflow.presentation

import com.fileflow.data.db.Database
import com.fileflow.data.db.HistoryRepository
import com.fileflow.data.db.RulesRepository
import com.fileflow.data.db.SettingsRepository
import com.fileflow.domain.model.OperationRecord
import com.fileflow.domain.model.ScanResult
import com.fileflow.domain.model.Settings
import com.fileflow.domain.model.SortRule
import com.fileflow.ui.localization.Language
import java.io.File

class AppStateHolder(val dataDir: File) {
    val settingsRepo = SettingsRepository()
    val historyRepo = HistoryRepository()
    val rulesRepo = RulesRepository()

    var settings: Settings = Settings()
        private set
    var scanResult: ScanResult? = null
    var history: List<OperationRecord> = emptyList()
    var rules: List<SortRule> = emptyList()

    fun init() {
        Database.init(File(dataDir, "fileflow.db"))
        settings = settingsRepo.load()
        history = historyRepo.list()
        rules = rulesRepo.list()
    }

    fun saveSettings(s: Settings) {
        settings = s
        settingsRepo.save(s)
    }

    fun refreshHistory() { history = historyRepo.list() }
    fun refreshRules() { rules = rulesRepo.list() }

    fun languageEnum(): Language = if (settings.language == "ru") Language.RU else Language.EN
}