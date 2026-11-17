@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.screens.produto

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
import org.greenhouse.app.viewmodel.ProdutoViewModel
import org.greenhouse.app.viewmodel.formatarPreco
import org.greenhouse.ui.components.BotaoPrincipal
import org.greenhouse.ui.components.BotaoSecundario
import org.greenhouse.ui.components.CampoAtivo
import org.greenhouse.ui.components.CardItem
import org.greenhouse.ui.components.MeuTextField
import org.greenhouse.ui.screens.produto.validation.formatarPrazoPreparoProduto
import org.greenhouse.ui.screens.produto.validation.formatarPrecoProduto
import org.greenhouse.ui.theme.AppTypography
import org.greenhouse.ui.theme.TextPrimary

@Composable
fun ProdutoFormulario(
    padding: PaddingValues,
    produtoViewModel: ProdutoViewModel,
) {
    val erros = produtoViewModel.erros.value
    val emEdicao = produtoViewModel.id.value.isNotEmpty()
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            if (emEdicao) "Editando produto ${produtoViewModel.id.value}" else "Novo produto",
            color = TextPrimary,
            style = AppTypography().headlineLarge,
        )
        MeuTextField(
            value = produtoViewModel.codigo.value,
            onValueChange = { produtoViewModel.codigo.value = it },
            label = "Código",
            placeholder = "Código do produto (opcional)",
            error = erros["codigo"],
        )
        MeuTextField(
            value = produtoViewModel.nome.value,
            onValueChange = { produtoViewModel.nome.value = it },
            label = "Nome *",
            placeholder = "Digite o nome do produto",
            error = erros["nome"],
        )
        MeuTextField(
            value = produtoViewModel.descricao.value,
            onValueChange = { produtoViewModel.descricao.value = it },
            label = "Descrição",
            placeholder = "Descreva o produto (opcional)",
            error = erros["descricao"],
            minLines = 3,
        )
        MeuTextField(
            value = produtoViewModel.preco.value,
            onValueChange = { produtoViewModel.preco.value = formatarPrecoProduto(it) },
            label = "Preço (R$) *",
            placeholder = "0,00",
            error = erros["preco"],
            keyboardType = KeyboardType.Decimal,
        )
        MeuTextField(
            value = produtoViewModel.prazoPreparoMinutos.value,
            onValueChange = { produtoViewModel.prazoPreparoMinutos.value = formatarPrazoPreparoProduto(it) },
            label = "Prazo de preparo (minutos)",
            placeholder = "Opcional",
            error = erros["prazoPreparoMinutos"],
            keyboardType = KeyboardType.Number,
        )
        CampoAtivo("Produto ativo", produtoViewModel.ativo.value) { produtoViewModel.ativo.value = it }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            BotaoPrincipal("Gravar", onClick = { produtoViewModel.gravar() }, modifier = Modifier.weight(1f))
            BotaoSecundario("Limpar campos", onClick = { produtoViewModel.limparCampos() }, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun ProdutoListagem(
    padding: PaddingValues,
    produtoViewModel: ProdutoViewModel,
) {
    val lista by produtoViewModel.lista.collectAsState()
    LaunchedEffect(Unit) { produtoViewModel.carregar() }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
        if (lista.isEmpty()) {
            item {
                Text("Nenhum produto cadastrado", color = TextPrimary, modifier = Modifier.padding(16.dp))
            }
        }
        items(lista, key = { it.id }) { produto ->
            CardItem(
                titulo = "${produto.id} - ${produto.nome}",
                linhas =
                    listOf(
                        "Código: ${produto.codigo ?: "-"}",
                        "Preço: R$ ${formatarPreco(produto.preco)}",
                        "Preparo: ${produto.prazoPreparoMinutos?.let { "$it min" } ?: "-"}",
                        if (produto.ativo) "Ativo" else "Inativo",
                    ),
                onEditar = { produtoViewModel.editar(produto) },
                onApagar = { produtoViewModel.apagar(produto.id) },
            )
        }
    }
}
