@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import greenhouse.shared.generated.resources.Res
import greenhouse.shared.generated.resources.icon_adicionar
import greenhouse.shared.generated.resources.icon_editar
import greenhouse.shared.generated.resources.icon_excluir
import greenhouse.shared.generated.resources.icon_pesquisa
import org.greenhouse.ui.theme.AppTypography
import org.greenhouse.ui.theme.GreenPrimary
import org.greenhouse.ui.theme.GreenSecondary
import org.greenhouse.ui.theme.Surface
import org.greenhouse.ui.theme.TextPrimary
import org.jetbrains.compose.resources.painterResource

@Composable
fun CardItem(
    titulo: String,
    linhas: List<String>,
    onEditar: () -> Unit,
    onApagar: () -> Unit,
) {
    val typography = AppTypography()
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(Surface, RoundedCornerShape(16.dp))
                .border(1.dp, GreenPrimary, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(titulo, color = TextPrimary, style = typography.titleMedium)
            for (linha in linhas) {
                Text(linha, color = TextPrimary, style = typography.bodyMedium)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = onEditar) {
                Image(painterResource(Res.drawable.icon_editar), contentDescription = "Editar", modifier = Modifier.size(28.dp))
            }
            IconButton(onClick = onApagar) {
                Image(painterResource(Res.drawable.icon_excluir), contentDescription = "Apagar", modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
fun CampoAtivo(
    texto: String,
    marcado: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = marcado,
            onCheckedChange = onChange,
            colors = CheckboxDefaults.colors(checkedColor = GreenPrimary, uncheckedColor = GreenPrimary),
        )
        Text(texto, color = TextPrimary, style = AppTypography().bodyMedium)
    }
}

@Composable
fun FormularioListagemBottomBar(
    formularioSelecionado: Boolean,
    onFormulario: () -> Unit,
    onListagem: () -> Unit,
) {
    val cores =
        NavigationBarItemDefaults.colors(
            selectedTextColor = GreenPrimary,
            unselectedTextColor = TextPrimary,
            indicatorColor = GreenSecondary.copy(alpha = 0.3f),
        )
    NavigationBar(containerColor = Surface) {
        NavigationBarItem(
            icon = { Image(painterResource(Res.drawable.icon_adicionar), contentDescription = null, modifier = Modifier.size(24.dp)) },
            label = { Text("Formulário") },
            selected = formularioSelecionado,
            onClick = onFormulario,
            colors = cores,
        )
        NavigationBarItem(
            icon = { Image(painterResource(Res.drawable.icon_pesquisa), contentDescription = null, modifier = Modifier.size(24.dp)) },
            label = { Text("Listagem") },
            selected = !formularioSelecionado,
            onClick = onListagem,
            colors = cores,
        )
    }
}
