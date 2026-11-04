package com.artistwalls.app.data.remote

import retrofit2.http.GET
import retrofit2.http.QueryMap

/**
 * Wikimedia Commons action API — the only image source in the app.
 * https://commons.wikimedia.org/w/api.php
 */
interface WikimediaApi {

    @GET("w/api.php")
    suspend fun query(@QueryMap params: Map<String, String>): CommonsResponse
}
