=== ABOUT: Carries the saved first-run notice through to the screen and lets it be dismissed for good ===
package com.aibuilt.jaiho777alwayswin.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aibuilt.jaiho777alwayswin.data.ModLog
import com.aibuilt.jaiho777alwayswin.data.PatchStatus
import com.aibuilt.jaiho777alwayswin.data.SettingsRepository
import com.aibuilt.jaiho777alwayswin.model.ModConfig
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val settings = SettingsRepository(app)
    val log = ModLog()

    val config: StateFlow<ModConfig> = settings.config.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        ModConfig()
    )

    val showIntro: StateFlow<Boolean> = settings.showIntro.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        true
    )

    val lines = log.lines
    val status = log.status

    fun setPatchEnabled(value: Boolean) = viewModelScope.launch { settings.setPatchEnabled(value) }
    fun setForceCode(value: Boolean) = viewModelScope.launch { settings.setForceCode(value) }
    fun setScaleWins(value: Boolean) = viewModelScope.launch { settings.setScaleWins(value) }
    fun setMultiplier(value: Int) = viewModelScope.launch { settings.setMultiplier(value) }
    fun setInjectWin(value: Boolean) = viewModelScope.launch { settings.setInjectWin(value) }
    fun setWinFactor(value: Int) = viewModelScope.launch { settings.setWinFactor(value) }
    fun setCoinFloor(value: Boolean) = viewModelScope.launch { settings.setCoinFloor(value) }
    fun setDefaultBet(value: Int) = viewModelScope.launch { settings.setDefaultBet(value) }
    fun setGameUrl(value: String) = viewModelScope.launch { settings.setGameUrl(value) }
    fun dismissIntro() = viewModelScope.launch { settings.setShowIntro(false) }

    fun resetSettings() = viewModelScope.launch {
        settings.resetToDefaults()
        log.add("Settings reset to defaults")
    }

    fun clearLog() = log.clear()

    fun noteInjection() {
        log.setStatus(PatchStatus.INJECTING)
    }

    fun noteReady() {
        log.setStatus(PatchStatus.ACTIVE)
        log.add("AlwaysWin is ON - every reply is now a win")
    }

    fun noteDisabled() {
        log.setStatus(PatchStatus.IDLE)
        log.add("Patch is switched off in settings")
    }

    fun noteFailed(reason: String) {
        log.setStatus(PatchStatus.FAILED)
        log.add("Patch could not attach: $reason")
    }

    fun note(text: String) {
        log.add(text)
    }

    fun clearStatus() {
        log.setStatus(PatchStatus.IDLE)
    }
}
