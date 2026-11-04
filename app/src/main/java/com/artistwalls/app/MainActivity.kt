package com.artistwalls.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.artistwalls.app.ui.navigation.ArtistWallsRoot
import com.artistwalls.app.ui.theme.AmoledBlack
import com.artistwalls.app.ui.theme.ArtistWallsTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            ArtistWallsTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = AmoledBlack) {
                    ArtistWallsRoot(container = (application as ArtistWallsApp).container)
                }
            }
        }
    }
}
