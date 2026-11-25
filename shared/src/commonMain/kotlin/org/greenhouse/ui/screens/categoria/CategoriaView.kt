@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.screens.categoria

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.greenhouse.app.viewmodel.CategoriaViewModel
import org.greenhouse.app.viewmodel.isoParaDataBrasileira
import org.greenhouse.ui.components.BotaoPrincipal
import org.greenhouse.ui.components.BotaoSecundario
import org.greenhouse.ui.components.CampoAtivo
import org.greenhouse.ui.components.CardItem
import org.greenhouse.ui.components.MeuTextField
import org.greenhouse.ui.screens.desconto.validation.formatarDataBrasileira
import org.greenhouse.ui.theme.AppTypography
import org.greenhouse.ui.theme.TextPrimary

@Composable
fun CategoriaFormulario(
    padding: PaddingValues,
    categoriaViewModel: CategoriaViewModel,
) {
    val erros = categoriaViewModel.erros.value
    val emEdicao = categoriaViewModel.id.value.isNotEmpty()
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            if (emEdicao) "Editando categoria ${categoriaViewModel.id.value}" else "Nova categoria",
            color = TextPrimary,
            style = AppTypography().headlineLarge,
        )
        MeuTextField(
            value = categoriaViewModel.nome.value,
            onValueChange = { categoriaViewModel.nome.value = it },
            label = "Nome *",
            placeholder = "Digite o nome da categoria",
            error = erros["nome"],
        )
        MeuTextField(
            value = categoriaViewModel.descricao.value,
            onValueChange = { categoriaViewModel.descricao.value = it },
            label = "Descrição *",
            placeholder = "Descreva a categoria",
            error = erros["descricao"],
            minLines = 3,
        )
        MeuTextField(
            value = categoriaViewModel.dataCadastro.value,
            onValueChange = { categoriaViewModel.dataCadastro.value = formatarDataBrasileira(it) },
            label = "Data de cadastro",
            placeholder = "DD/MM/AAAA (opcional)",
            error = erros["dataCadastro"],
            keyboardType = KeyboardType.Number,
        )
        MeuTextField(
            value = categoriaViewModel.observacao.value,
            onValueChange = { categoriaViewModel.observacao.value = it },
            label = "Observação",
            placeholder = "Observação (opcional)",
            error = erros["observacao"],
            minLines = 2,
        )
        CampoAtivo("Categoria ativa", categoriaViewModel.ativo.value) { categoriaViewModel.ativo.value = it }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            BotaoPrincipal("Gravar", onClick = { categoriaViewModel.gravar() }, modifier = Modifier.weight(1f))
            BotaoSecundario("Limpar campos", onClick = { categoriaViewModel.limparCampos() }, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun CategoriaListagem(
    padding: PaddingValues,
    categoriaViewModel: CategoriaViewModel,
) {
    val lista by categoriaViewModel.lista.collectAsState()
    LaunchedEffect(Unit) { categoriaViewModel.carregar() }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
        if (lista.isEmpty()) {
            item {
                Text("Nenhuma categoria cadastrada", color = TextPrimary, modifier = Modifier.padding(16.dp))
            }
        }
        items(lista, key = { it.id }) { categoria ->
            CardItem(
                titulo = "${categoria.id} - ${categoria.nome}",
                linhas =
                    listOf(
                        "Descrição: ${categoria.descricao}",
                        "Cadastro: ${isoParaDataBrasileira(categoria.dataCadastro)}",
                        if (categoria.ativo) "Ativa" else "Inativa",
                    ),
                onEditar = { categoriaViewModel.editar(categoria) },
                onApagar = { categoriaViewModel.apagar(categoria.id) },
            )
        }
    }
}
