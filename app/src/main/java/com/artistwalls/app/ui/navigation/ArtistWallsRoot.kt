package com.artistwalls.app.ui.navigation

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.artistwalls.app.di.AppContainer
import com.artistwalls.app.di.LocalAppContainer
import com.artistwalls.app.ui.components.glass
import com.artistwalls.app.ui.screens.artist.ArtistDetailScreen
import com.artistwalls.app.ui.screens.category.CategoryScreen
import com.artistwalls.app.ui.screens.credits.CreditsScreen
import com.artistwalls.app.ui.screens.home.HomeScreen
import com.artistwalls.app.ui.screens.library.LibraryScreen
import com.artistwalls.app.ui.screens.search.SearchScreen
import com.artistwalls.app.ui.screens.wallpaper.WallpaperDetailScreen
import com.artistwalls.app.ui.theme.Gold
import com.artistwalls.app.ui.theme.TextTertiary

object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val LIBRARY = "library"
    const val CREDITS = "credits"

    const val ARTIST = "artist/{slug}"
    const val CATEGORY = "category/{id}"
    const val WALLPAPER = "wallpaper/{id}"

    fun artist(slug: String) = "artist/$slug"
    fun category(id: String) = "category/$id"
    fun wallpaper(id: String) = "wallpaper/${Uri.encode(id)}"
}

private data class BottomItem(val route: String, val label: String, val icon: ImageVector)

private val bottomItems = listOf(
    BottomItem(Routes.HOME, "Home", Icons.Rounded.Home),
    BottomItem(Routes.SEARCH, "Search", Icons.Rounded.Search),
    BottomItem(Routes.LIBRARY, "Library", Icons.Rounded.Favorite),
    BottomItem(Routes.CREDITS, "Credits", Icons.Rounded.Info)
)

@Composable
fun ArtistWallsRoot(container: AppContainer) {
    CompositionLocalProvider(LocalAppContainer provides container) {
        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route
        val showBar = bottomItems.any { it.route == currentRoute }

        Box(modifier = Modifier.fillMaxSize().background(com.artistwalls.app.ui.theme.AmoledBlack)) {
            NavHost(
                navController = navController,
                startDestination = Routes.HOME,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Routes.HOME) {
                    HomeScreen(
                        onOpenArtist = { navController.navigate(Routes.artist(it)) },
                        onOpenWallpaper = { navController.navigate(Routes.wallpaper(it)) },
                        onOpenCategory = { navController.navigate(Routes.category(it)) },
                        onOpenSearch = { navController.navigate(Routes.SEARCH) },
                        onOpenCredits = { navController.navigate(Routes.CREDITS) }
                    )
                }
                composable(Routes.SEARCH) {
                    SearchScreen(
                        onOpenArtist = { navController.navigate(Routes.artist(it)) },
                        onOpenWallpaper = { navController.navigate(Routes.wallpaper(it)) },
                        onOpenCategory = { navController.navigate(Routes.category(it)) }
                    )
                }
                composable(Routes.LIBRARY) {
                    LibraryScreen(
                        onOpenWallpaper = { navController.navigate(Routes.wallpaper(it)) },
                        onOpenSearch = { navController.navigate(Routes.SEARCH) }
                    )
                }
                composable(Routes.CREDITS) {
                    CreditsScreen(
                        onOpenWallpaper = { navController.navigate(Routes.wallpaper(it)) }
                    )
                }
                composable(
                    route = Routes.ARTIST,
                    arguments = listOf(navArgument("slug") { type = NavType.StringType })
                ) { entry ->
                    ArtistDetailScreen(
                        slug = entry.arguments?.getString("slug").orEmpty(),
                        onBack = { navController.popBackStack() },
                        onOpenWallpaper = { navController.navigate(Routes.wallpaper(it)) },
                        onOpenArtist = { navController.navigate(Routes.artist(it)) }
                    )
                }
                composable(
                    route = Routes.CATEGORY,
                    arguments = listOf(navArgument("id") { type = NavType.StringType })
                ) { entry ->
                    CategoryScreen(
                        categoryId = entry.arguments?.getString("id").orEmpty(),
                        onBack = { navController.popBackStack() },
                        onOpenWallpaper = { navController.navigate(Routes.wallpaper(it)) }
                    )
                }
                composable(
                    route = Routes.WALLPAPER,
                    arguments = listOf(navArgument("id") { type = NavType.StringType })
                ) { entry ->
                    val raw = entry.arguments?.getString("id").orEmpty()
                    WallpaperDetailScreen(
                        wallpaperId = Uri.decode(raw),
                        onBack = { navController.popBackStack() },
                        onOpenArtist = { navController.navigate(Routes.artist(it)) },
                        onOpenCredits = { navController.navigate(Routes.CREDITS) }
                    )
                }
            }

            if (showBar) {
                ArtistWallsBottomBar(
                    currentRoute = currentRoute,
                    onSelect = { route ->
                        if (route != currentRoute) {
                            navController.navigate(route) {
                                popUpTo(Routes.HOME) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun ArtistWallsBottomBar(
    currentRoute: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .glass(RoundedCornerShape(24.dp))
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        bottomItems.forEach { item ->
            val selected = item.route == currentRoute
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onSelect(item.route) }
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = if (selected) Gold else TextTertiary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) Gold else TextTertiary,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
        }
    }
}
