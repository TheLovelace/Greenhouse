package org.greenhouse

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.greenhouse.ui.screens.desconto.DescontoCadastro
import org.greenhouse.ui.screens.desconto.model.DescontoCadastroData
import org.greenhouse.ui.screens.estoque.EstoqueCadastro
import org.greenhouse.ui.screens.estoque.model.EstoqueCadastroData
import org.greenhouse.ui.screens.home.HomeScreen
import org.greenhouse.ui.screens.home.HomeDestination
import org.greenhouse.ui.theme.GreenhouseTheme

@Composable
fun App(
    onNavigate: (HomeDestination) -> Unit = {},
    onSaveDesconto: (DescontoCadastroData) -> Unit = {},
    onSaveEstoque: (EstoqueCadastroData) -> Unit = {},
) {
    var isDescontoCadastroVisible by remember { mutableStateOf(false) }
    var isEstoqueCadastroVisible by remember { mutableStateOf(false) }

    GreenhouseTheme {
        if (isDescontoCadastroVisible) {
            DescontoCadastro(
                onBack = { isDescontoCadastroVisible = false },
                onSave = onSaveDesconto,
            )
        } else if (isEstoqueCadastroVisible) {
            EstoqueCadastro(
                onBack = { isEstoqueCadastroVisible = false },
                onSave = onSaveEstoque,
            )
        } else {
            HomeScreen(
                onNavigate = { destination ->
                    when (destination) {
                        HomeDestination.DESCONTOS -> isDescontoCadastroVisible = true
                        HomeDestination.ESTOQUE -> isEstoqueCadastroVisible = true
                        else -> onNavigate(destination)
                    }
                },
            )
        }
    }
}