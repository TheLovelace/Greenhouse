package org.greenhouse.app.repository

actual fun createAuthTokenStorage(): AuthTokenStorage = DesktopAuthTokenStorage
