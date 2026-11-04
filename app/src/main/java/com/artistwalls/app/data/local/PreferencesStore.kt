package com.artistwalls.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import com.artistwalls.app.data.model.FxSettings
import com.artistwalls.app.data.model.FxValues
import kotlinx.coroutines.flow.combine
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "artistwalls_prefs")

/** Small, non-critical preferences: recent searches and display toggles. */
class PreferencesStore(
    private val context: Context,
    private val json: Json
) {

    private val RECENT_SEARCHES = stringPreferencesKey("recent_searches")
    private val SHOW_LOW_RES = booleanPreferencesKey("show_low_res")
    private val FX_GLASS = booleanPreferencesKey("fx_glass")
    private val FX_DEPTH = booleanPreferencesKey("fx_depth")
    private val FX_BOUNCE = booleanPreferencesKey("fx_bounce")
    private val FX_MAC = booleanPreferencesKey("fx_mac")
    private val FXV_TINT = floatPreferencesKey("fxv_tint")
    private val FXV_RIM = floatPreferencesKey("fxv_rim")
    private val FXV_TILT = floatPreferencesKey("fxv_tilt")
    private val FXV_LIFT = floatPreferencesKey("fxv_lift")
    private val FXV_SHADOW = floatPreferencesKey("fxv_shadow")
    private val FXV_PARALLAX = floatPreferencesKey("fxv_parallax")
    private val FXV_GLARE = floatPreferencesKey("fxv_glare")
    private val FXV_BS = floatPreferencesKey("fxv_bspeed")
    private val FXV_BA = floatPreferencesKey("fxv_bamt")
    private val FXV_MD = floatPreferencesKey("fxv_mdur")
    private val FXV_MS = floatPreferencesKey("fxv_msize")

    val recentSearches: Flow<List<String>> = context.dataStore.data.map { prefs ->
        decode(prefs[RECENT_SEARCHES])
    }

    val showLowResolution: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[SHOW_LOW_RES] ?: true
    }

    /** The four visual-effect switches; every one defaults to on. */
    val fxSettings: Flow<FxSettings> = context.dataStore.data.map { prefs ->
        val defaults = FxValues()
        FxSettings(
            glass = prefs[FX_GLASS] ?: true,
            depth = prefs[FX_DEPTH] ?: true,
            bounce = prefs[FX_BOUNCE] ?: true,
            mac = prefs[FX_MAC] ?: true,
            values = FxValues(
                glassTint = prefs[FXV_TINT] ?: defaults.glassTint,
                glassRim = prefs[FXV_RIM] ?: defaults.glassRim,
                tilt = prefs[FXV_TILT] ?: defaults.tilt,
                lift = prefs[FXV_LIFT] ?: defaults.lift,
                shadow = prefs[FXV_SHADOW] ?: defaults.shadow,
                parallax = prefs[FXV_PARALLAX] ?: defaults.parallax,
                glare = prefs[FXV_GLARE] ?: defaults.glare,
                bounceDuration = prefs[FXV_BS] ?: defaults.bounceDuration,
                bounceAmount = prefs[FXV_BA] ?: defaults.bounceAmount,
                macDuration = prefs[FXV_MD] ?: defaults.macDuration,
                macSize = prefs[FXV_MS] ?: defaults.macSize
            )
        )
    }

    suspend fun saveFx(settings: FxSettings) {
        context.dataStore.edit { prefs ->
            prefs[FX_GLASS] = settings.glass
            prefs[FX_DEPTH] = settings.depth
            prefs[FX_BOUNCE] = settings.bounce
            prefs[FX_MAC] = settings.mac
            val v = settings.values
            prefs[FXV_TINT] = v.glassTint
            prefs[FXV_RIM] = v.glassRim
            prefs[FXV_TILT] = v.tilt
            prefs[FXV_LIFT] = v.lift
            prefs[FXV_SHADOW] = v.shadow
            prefs[FXV_PARALLAX] = v.parallax
            prefs[FXV_GLARE] = v.glare
            prefs[FXV_BS] = v.bounceDuration
            prefs[FXV_BA] = v.bounceAmount
            prefs[FXV_MD] = v.macDuration
            prefs[FXV_MS] = v.macSize
        }
    }

    suspend fun addSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.length < 2) return
        context.dataStore.edit { prefs ->
            val current = decode(prefs[RECENT_SEARCHES])
            val next = (listOf(trimmed) + current.filterNot { it.equals(trimmed, true) }).take(8)
            prefs[RECENT_SEARCHES] = json.encodeToString(next)
        }
    }

    suspend fun clearSearches() {
        context.dataStore.edit { it.remove(RECENT_SEARCHES) }
    }

    suspend fun setShowLowResolution(show: Boolean) {
        context.dataStore.edit { it[SHOW_LOW_RES] = show }
    }

    private fun decode(raw: String?): List<String> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching { json.decodeFromString<List<String>>(raw) }.getOrDefault(emptyList())
    }
}
