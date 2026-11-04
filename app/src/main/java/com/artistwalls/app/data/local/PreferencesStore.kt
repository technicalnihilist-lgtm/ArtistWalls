package com.artistwalls.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "artistwalls_prefs")

/** Small, non-critical preferences: recent searches and display toggles. */
class PreferencesStore(
    private val context: Context,
    private val json: Json
) {

    private val RECENT_SEARCHES = stringPreferencesKey("recent_searches")
    private val SHOW_LOW_RES = booleanPreferencesKey("show_low_res")

    val recentSearches: Flow<List<String>> = context.dataStore.data.map { prefs ->
        decode(prefs[RECENT_SEARCHES])
    }

    val showLowResolution: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[SHOW_LOW_RES] ?: true
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
