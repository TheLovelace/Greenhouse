package org.greenhouse

import androidx.compose.runtime.Composable
import org.greenhouse.ui.screens.home.HomeScreen
import org.greenhouse.ui.screens.home.HomeDestination
import org.greenhouse.ui.theme.GreenhouseTheme

@Composable
fun App(onNavigate: (HomeDestination) -> Unit = {}) {
    GreenhouseTheme {
        HomeScreen(onNavigate = onNavigate)
    }
}