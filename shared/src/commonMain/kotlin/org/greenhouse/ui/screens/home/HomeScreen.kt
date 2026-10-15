package org.greenhouse.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import greenhouse.shared.generated.resources.Res
import greenhouse.shared.generated.resources.icon_alerta
import greenhouse.shared.generated.resources.icon_categoria
import greenhouse.shared.generated.resources.icon_cliente
import greenhouse.shared.generated.resources.icon_coffee
import greenhouse.shared.generated.resources.icon_compra
import greenhouse.shared.generated.resources.icon_desconto
import greenhouse.shared.generated.resources.icon_especie
import greenhouse.shared.generated.resources.icon_estoque
import greenhouse.shared.generated.resources.icon_forma_pagamento
import greenhouse.shared.generated.resources.icon_funcionario
import greenhouse.shared.generated.resources.icon_fornecedor
import greenhouse.shared.generated.resources.icon_pedido
import greenhouse.shared.generated.resources.icon_produto
import org.greenhouse.ui.theme.Background
import org.greenhouse.ui.theme.AppTypography
import org.greenhouse.ui.theme.GreenPrimary
import org.greenhouse.ui.theme.Surface
import org.greenhouse.ui.theme.TextPrimary
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

enum class HomeDestination {
    ALERTAS,
    CATEGORIAS,
    CLIENTES,
    COMPRAS,
    DESCONTOS,
    ESPECIES,
    ESTOQUE,
    FORMAS_PAGAMENTO,
    FUNCIONARIOS,
    FORNECEDORES,
    PEDIDOS,
    PRODUTOS,
}

private data class HomeMenuItem(
    val label: String,
    val icon: DrawableResource,
    val destination: HomeDestination,
)

private val homeMenuItems =
    listOf(
        HomeMenuItem("Clientes", Res.drawable.icon_cliente, HomeDestination.CLIENTES),
        HomeMenuItem("Funcionários", Res.drawable.icon_funcionario, HomeDestination.FUNCIONARIOS),
        HomeMenuItem("Fornecedores", Res.drawable.icon_fornecedor, HomeDestination.FORNECEDORES),
        HomeMenuItem("Produtos", Res.drawable.icon_produto, HomeDestination.PRODUTOS),
        HomeMenuItem("Categorias", Res.drawable.icon_categoria, HomeDestination.CATEGORIAS),
        HomeMenuItem("Pedidos", Res.drawable.icon_pedido, HomeDestination.PEDIDOS),
        HomeMenuItem("Compras", Res.drawable.icon_compra, HomeDestination.COMPRAS),
        HomeMenuItem("Espécies", Res.drawable.icon_especie, HomeDestination.ESPECIES),
        HomeMenuItem("Estoque", Res.drawable.icon_estoque, HomeDestination.ESTOQUE),
        HomeMenuItem("Formas de pagamento", Res.drawable.icon_forma_pagamento, HomeDestination.FORMAS_PAGAMENTO),
        HomeMenuItem("Descontos", Res.drawable.icon_desconto, HomeDestination.DESCONTOS),            
        HomeMenuItem("Alertas", Res.drawable.icon_alerta, HomeDestination.ALERTAS),
    )

    
@Composable
fun HomeScreen(
    onNavigate: (HomeDestination) -> Unit,
) {
    val typography = AppTypography()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Background)
                .padding(horizontal = 30.dp, vertical = 30.dp),
    ) {
        Text(
            text =
                buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            fontFamily = typography.titleLarge.fontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 30.sp,
                            color = TextPrimary,
                        ),
                    ) {
                        append("Bem-vindo(a) à ")
                    }
                    withStyle(
                        SpanStyle(
                            fontFamily = typography.displayLarge.fontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 38.sp,
                            color = TextPrimary,
                        ),
                    ) {
                        append("Greenhouse!")
                    }
                },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(30.dp))

        Text(
            text = "Utilize Greenhouse para gerenciar sua floricultura. Comece pelos botões abaixo para aplicar cadastros.",
            modifier = Modifier.fillMaxWidth(),
            color = TextPrimary,
            style = typography.bodyLarge.copy(lineHeight = 24.sp),
            textAlign = TextAlign.Justify,
        )

        Spacer(Modifier.height(24.dp))

        BoxWithConstraints(modifier = Modifier.fillMaxWidth().weight(1f)) {
            val columnCount = (maxWidth / 256.dp).toInt().coerceIn(1, 4)

            LazyVerticalGrid(
                columns = GridCells.Fixed(columnCount),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 8.dp),
            ) {
                items(homeMenuItems) { item ->
                    Button(
                        onClick = { onNavigate(item.destination) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 84.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, GreenPrimary),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = Surface,
                                contentColor = TextPrimary,
                            ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Image(
                                painter = painterResource(item.icon),
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                            )
                            Text(
                                text = item.label,
                                modifier = Modifier.weight(1f),
                                style = typography.titleMedium,
                                color = TextPrimary,
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        val footerInlineContent =
            mapOf(
                "coffee" to
                    InlineTextContent(
                        Placeholder(
                            width = 20.sp,
                            height = 20.sp,
                            placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
                        ),
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.icon_coffee),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                        )
                    },
            )

        Text(
            text =
                buildAnnotatedString {
                    withStyle(SpanStyle(fontSize = 14.sp, color = TextPrimary)) {
                        append("\u00A9")
                    }
                    append(" Copyright Greenhouse 2026. All Rights Reserved. Make with ")
                    appendInlineContent("coffee", "[coffee]")
                },
            modifier = Modifier.fillMaxWidth(),
            color = TextPrimary,
            style = typography.bodySmall,
            textAlign = TextAlign.Center,
            inlineContent = footerInlineContent,
        )
    }
}
