package com.artistwalls.app

import com.artistwalls.app.data.catalog.ArtistCatalog
import com.artistwalls.app.data.remote.WikimediaApi
import com.artistwalls.app.data.repo.WallpaperRepository
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URI
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Test
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Runs the real repository against the live Wikimedia Commons API on the JVM.
 * This is the same code the app ships, so it tells us whether the data path or the
 * image-loading layer is at fault.
 */
class LiveApiTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        explicitNulls = false
    }

    private fun client(ua: String?) = OkHttpClient.Builder()
        .apply {
            // this test JVM sits behind the sandbox egress proxy; the phone does not
            val raw = System.getenv("https_proxy") ?: System.getenv("HTTPS_PROXY")
            if (raw != null) {
                val uri = URI(raw)
                proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress(uri.host, uri.port)))
                val user = uri.userInfo?.substringBefore(':') ?: ""
                val pass = uri.userInfo?.substringAfter(':', "") ?: ""
                proxyAuthenticator { _, response ->
                    response.request.newBuilder()
                        .header("Proxy-Authorization", Credentials.basic(user, pass))
                        .build()
                }
            }
        }
        .addInterceptor { chain ->
            val b = chain.request().newBuilder()
            if (ua != null) b.header("User-Agent", ua)
            chain.proceed(b.build())
        }
        .build()

    private fun api(ua: String?): WikimediaApi = Retrofit.Builder()
        .baseUrl("https://commons.wikimedia.org/")
        .client(client(ua))
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(WikimediaApi::class.java)

    private val appUa = "ArtistWalls/1.0 (Android; wallpaper app; contact: nihaonihao54059@gmail.com)"

    @Test
    fun artistFeedAndImageFetch() = runBlocking {
        val repository = WallpaperRepository(api(appUa))
        val artist = ArtistCatalog.bySlug("arijit-singh")!!
        val page = try {
            repository.artistFeed(artist, offset = 0, limit = 12)
        } catch (e: Throwable) {
            var c: Throwable? = e
            while (c != null) { println("TEST >> cause: ${c.javaClass.name}: ${c.message}"); c = c.cause }
            throw e
        }
        println("TEST >> feed items=${page.items.size} hasMore=${page.hasMore} rejected=${page.rejectedCount}")

        page.items.take(3).forEach { w ->
            println("TEST >> title=${w.title}")
            println("TEST >>   thumb=${w.thumbUrl}")
            println("TEST >>   quality=${w.quality} ${w.resolution} artist=${w.artistName}")
            val resp = client(appUa).newCall(Request.Builder().url(w.thumbUrl).build()).execute()
            val body = resp.body
            val head = body?.byteStream()?.let { s ->
                val buf = ByteArray(8); val n = s.read(buf); buf.copyOf(maxOf(n, 0))
            }
            println("TEST >>   image HTTP ${resp.code} type=${body?.contentType()} bytes=${body?.contentLength()} magic=${head?.joinToString(" ") { b -> "%02X".format(b) }}")
        }
        check(page.items.isNotEmpty()) { "repository returned no wallpapers" }
    }

    @Test
    fun everyFeedDecodes() = runBlocking {
        val repository = WallpaperRepository(api(appUa))
        val category = ArtistCatalog.categoryById("legendary-artists")!!

        suspend fun report(name: String, block: suspend () -> Int) {
            val count = try { block() } catch (e: Throwable) {
                var c: Throwable? = e
                while (c != null) { println("TEST >> $name cause: ${c.javaClass.simpleName}: ${c.message}"); c = c.cause }
                -1
            }
            println("TEST >> feed $name -> $count items")
            check(count > 0) { "$name returned $count" }
        }

        report("artist:shreya") { repository.artistFeed(ArtistCatalog.bySlug("shreya-ghoshal")!!, 0, 12).items.size }
        report("artist:taylor") { repository.artistFeed(ArtistCatalog.bySlug("taylor-swift")!!, 0, 12).items.size }
        report("category:legendary") { repository.categoryFeed(category, 0, 12).items.size }
        report("trending") { repository.trending(0, 12).items.size }
        report("recent") { repository.recentlyAdded(0, 12).items.size }
        report("search:adele") { repository.searchWallpapers("Adele", 0, 12).items.size }
        report("single-file") { if (repository.wallpaperByTitle("File:Arijit Singh.jpg") != null) 1 else 0 }
        report("daily") { if (repository.dailyWallpaper() != null) 1 else 0 }
        report("page2") { repository.artistFeed(ArtistCatalog.bySlug("arijit-singh")!!, 12, 12).items.size }
    }

    @Test
    fun imageFetchWithGenericUserAgent() = runBlocking {
        val repository = WallpaperRepository(api(appUa))
        val page = repository.artistFeed(ArtistCatalog.bySlug("arijit-singh")!!, 0, 3)
        val url = page.items.first().thumbUrl
        for (ua in listOf(appUa, "okhttp/4.12.0")) {
            val resp = client(ua).newCall(Request.Builder().url(url).build()).execute()
            println("TEST >> UA=[${ua.take(20)}] HTTP ${resp.code} type=${resp.body?.contentType()}")
        }
    }
}
