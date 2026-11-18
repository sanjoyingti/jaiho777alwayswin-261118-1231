package com.aibuilt.jaiho777alwayswin.model

import org.json.JSONObject

/**
 * Everything the injected patch reads. Persisted with DataStore so the choices survive a restart.
 */
data class ModConfig(
    val patchEnabled: Boolean = true,
    val forceCode: Boolean = true,
    val scaleWins: Boolean = true,
    val multiplier: Int = 5,
    val injectWin: Boolean = true,
    val winFactor: Int = 3,
    val coinFloor: Boolean = true,
    val defaultBet: Int = 100,
    val gameUrl: String = DEFAULT_URL
) {
    fun toPatchJson(): String = JSONObject().apply {
        put("patchEnabled", patchEnabled)
        put("forceCode", forceCode)
        put("scaleWins", scaleWins)
        put("multiplier", multiplier.coerceIn(MULTIPLIER_RANGE))
        put("injectWin", injectWin)
        put("winFactor", winFactor.coerceIn(WIN_FACTOR_RANGE))
        put("coinFloor", coinFloor)
        put("defaultBet", defaultBet.coerceAtLeast(0))
        put("gameUrl", gameUrl.trim())
    }.toString()

    companion object {
        const val DEFAULT_URL = "https://www.jaiho777.com"

        val MULTIPLIER_RANGE = 1..20
        val WIN_FACTOR_RANGE = 1..10
    }
}
