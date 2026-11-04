package com.artistwalls.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.artistwalls.app.di.AppContainer

class ArtistWallsApp : Application(), ImageLoaderFactory {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }

    /**
     * A generous image cache: the gallery is entirely remote photography, so a warm
     * disk cache is what makes scrolling and offline favourites feel instant.
     */
    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        // Wikimedia's image servers reject requests that carry a generic library User-Agent
        // (OkHttp's default `okhttp/4.12.0` gets HTTP 403). Reusing the app's client means every
        // photograph goes out with the same descriptive User-Agent the API calls use.
        .okHttpClient { container.okHttpClient }
        .memoryCache {
            MemoryCache.Builder(this)
                .maxSizePercent(0.25)
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(cacheDir.resolve("image_cache"))
                .maxSizeBytes(320L * 1024 * 1024)
                .build()
        }
        .crossfade(200)
        .build()
}
