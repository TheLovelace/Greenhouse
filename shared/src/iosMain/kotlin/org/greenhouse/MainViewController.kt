package org.greenhouse

import androidx.compose.ui.window.ComposeUIViewController
import org.greenhouse.app.repository.IosAuthTokenStorage

fun MainViewController() = ComposeUIViewController { App(authTokenStorage = IosAuthTokenStorage) }