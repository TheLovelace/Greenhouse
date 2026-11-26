package org.greenhouse

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.greenhouse.app.repository.DesktopAuthTokenStorage

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Greenhouse",
        icon = painterResource("loading_flower.png"),
    ) {
        App(authTokenStorage = DesktopAuthTokenStorage)
    }
}