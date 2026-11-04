package com.artistwalls.app.data.remote

import com.artistwalls.app.data.catalog.ArtistCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Search suggestions come from two places, both live:
 *  1. the curated catalogue (instant, offline, exact artist names);
 *  2. Wikipedia's OpenSearch endpoint, so an artist who is not in the catalogue can
 *     still be found and searched on Commons.
 */
class SuggestionService(
    private val client: OkHttpClient,
    private val json: Json
) {

    suspend fun suggest(prefix: String, limit: Int = 8): List<String> = withContext(Dispatchers.IO) {
        val query = prefix.trim()
        if (query.length < 2) return@withContext emptyList()

        val local = ArtistCatalog.suggest(query, limit).map { it.name }
        if (local.size >= limit) return@withContext local

        val remote = runCatching { wikipediaOpenSearch(query, limit) }.getOrDefault(emptyList())
        (local + remote).distinct().take(limit)
    }

    private fun wikipediaOpenSearch(prefix: String, limit: Int): List<String> {
        val url = "https://en.wikipedia.org/w/api.php?action=opensearch&format=json" +
            "&namespace=0&limit=$limit&search=" + java.net.URLEncoder.encode(prefix, "UTF-8")
        val request = Request.Builder().url(url).get().build()
        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful || body.isBlank()) return emptyList()
            val root = json.parseToJsonElement(body)
            if (root !is JsonArray || root.size < 2) return emptyList()
            return root[1].jsonArray.mapNotNull { it.jsonPrimitive.contentOrNull }
        }
    }
}
