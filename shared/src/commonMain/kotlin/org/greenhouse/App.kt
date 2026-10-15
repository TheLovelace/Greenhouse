package org.greenhouse

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.greenhouse.ui.screens.desconto.DescontoCadastro
import org.greenhouse.ui.screens.desconto.model.DescontoCadastroData
import org.greenhouse.ui.screens.home.HomeScreen
import org.greenhouse.ui.screens.home.HomeDestination
import org.greenhouse.ui.theme.GreenhouseTheme

@Composable
fun App(
    onNavigate: (HomeDestination) -> Unit = {},
    onSaveDesconto: (DescontoCadastroData) -> Unit = {},
) {
    var isDescontoCadastroVisible by remember { mutableStateOf(false) }

    GreenhouseTheme {
        if (isDescontoCadastroVisible) {
            DescontoCadastro(
                onBack = { isDescontoCadastroVisible = false },
                onSave = onSaveDesconto,
            )
        } else {
            HomeScreen(
                onNavigate = { destination ->
                    if (destination == HomeDestination.DESCONTOS) {
                        isDescontoCadastroVisible = true
                    } else {
                        onNavigate(destination)
                    }
                },
            )
        }
    }
}