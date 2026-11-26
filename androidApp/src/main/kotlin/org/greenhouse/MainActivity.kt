package org.greenhouse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.greenhouse.app.repository.AndroidAuthTokenStorage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val authTokenStorage = AndroidAuthTokenStorage(applicationContext)

        setContent {
            App(authTokenStorage = authTokenStorage)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}