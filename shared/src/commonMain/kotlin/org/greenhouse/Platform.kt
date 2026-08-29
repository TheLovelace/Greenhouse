package org.greenhouse

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform