// Saves every switch, multiplier and address, so choices survive a restart.
package com.aibuilt.jaiho777alwayswin.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aibuilt.jaiho777alwayswin.model.ModConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsStore: DataStore<Preferences> by preferencesDataStore(name = "alwayswin_settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val patchEnabled = booleanPreferencesKey("patch_enabled")
        val forceCode = booleanPreferencesKey("force_code")
        val scaleWins = booleanPreferencesKey("scale_wins")
        val multiplier = intPreferencesKey("multiplier")
        val injectWin = booleanPreferencesKey("inject_win")
        val winFactor = intPreferencesKey("win_factor")
        val coinFloor = booleanPreferencesKey("coin_floor")
        val defaultBet = intPreferencesKey("default_bet")
        val gameUrl = stringPreferencesKey("game_url")
        val showIntro = booleanPreferencesKey("show_intro")
    }

    val config: Flow<ModConfig> = context.settingsStore.data.map { prefs ->
        val defaults = ModConfig()
        ModConfig(
            patchEnabled = prefs[Keys.patchEnabled] ?: defaults.patchEnabled,
            forceCode = prefs[Keys.forceCode] ?: defaults.forceCode,
            scaleWins = prefs[Keys.scaleWins] ?: defaults.scaleWins,
            multiplier = prefs[Keys.multiplier] ?: defaults.multiplier,
            injectWin = prefs[Keys.injectWin] ?: defaults.injectWin,
            winFactor = prefs[Keys.winFactor] ?: defaults.winFactor,
            coinFloor = prefs[Keys.coinFloor] ?: defaults.coinFloor,
            defaultBet = prefs[Keys.defaultBet] ?: defaults.defaultBet,
            gameUrl = prefs[Keys.gameUrl] ?: defaults.gameUrl
        )
    }

    val showIntro: Flow<Boolean> = context.settingsStore.data.map { prefs ->
        prefs[Keys.showIntro] ?: true
    }

    suspend fun setPatchEnabled(value: Boolean) = write { it[Keys.patchEnabled] = value }
    suspend fun setForceCode(value: Boolean) = write { it[Keys.forceCode] = value }
    suspend fun setScaleWins(value: Boolean) = write { it[Keys.scaleWins] = value }
    suspend fun setMultiplier(value: Int) = write { it[Keys.multiplier] = value }
    suspend fun setInjectWin(value: Boolean) = write { it[Keys.injectWin] = value }
    suspend fun setWinFactor(value: Int) = write { it[Keys.winFactor] = value }
    suspend fun setCoinFloor(value: Boolean) = write { it[Keys.coinFloor] = value }
    suspend fun setDefaultBet(value: Int) = write { it[Keys.defaultBet] = value }
    suspend fun setGameUrl(value: String) = write { it[Keys.gameUrl] = value }
    suspend fun setShowIntro(value: Boolean) = write { it[Keys.showIntro] = value }

    suspend fun resetToDefaults() {
        context.settingsStore.edit { prefs ->
            prefs.clear()
        }
    }

    private suspend fun write(block: (MutablePreferences) -> Unit) {
        context.settingsStore.edit { prefs -> block(prefs) }
    }
}
