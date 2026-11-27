package org.greenhouse.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.greenhouse.app.model.CompraFornecedor
import org.greenhouse.app.model.CompraFornecedorCreateRequest
import org.greenhouse.app.model.CompraFornecedorUpdateRequest
import org.greenhouse.app.model.CompraItemRequest
import org.greenhouse.app.repository.CompraFornecedorRepositorioRemoto
import org.greenhouse.app.repository.mensagemDeErro
import org.greenhouse.ui.screens.compraFornecedor.model.CompraFornecedorCadastroData
import org.greenhouse.ui.screens.compraFornecedor.model.CompraItemCadastroData
import org.greenhouse.ui.screens.compraFornecedor.model.StatusCompraCadastro

class CompraFornecedorViewModel(
    private val repositorio: CompraFornecedorRepositorioRemoto,
) : ViewModel() {
    private val _lista = MutableStateFlow<List<CompraFornecedor>>(emptyList())
    val lista = _lista.asStateFlow()

    var compraSelecionada by mutableStateOf<CompraFornecedor?>(null)
        private set
    var exibindoFormulario by mutableStateOf(false)
        private set
    var isCarregando by mutableStateOf(false)
        private set
    var isSalvando by mutableStateOf(false)
        private set
    var mensagemErro by mutableStateOf<String?>(null)
        private set
    var busca by mutableStateOf("")
        private set

    private var carregamento: Job? = null

    fun pesquisar(observacao: String) {
        if (isSalvando) return
        busca = observacao
        carregar(aguardarDigitacao = true)
    }

    fun carregar() {
        carregar(aguardarDigitacao = false)
    }

    private fun carregar(aguardarDigitacao: Boolean) {
        carregamento?.cancel()
        isCarregando = true
        mensagemErro = null
        val observacao = busca.trim().ifEmpty { null }
        carregamento =
            viewModelScope.launch {
                try {
                    if (aguardarDigitacao) delay(300)
                    _lista.value = repositorio.listar(observacao)
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    val mensagem = mensagemDeErro(exception, "Não foi possível carregar as compras")
                    if (isActive) mensagemErro = mensagem
                } finally {
                    if (isActive) isCarregando = false
                }
            }
    }

    fun novo() {
        compraSelecionada = null
        limparErro()
        exibindoFormulario = true
    }

    fun consultar(id: Long) {
        executar("Não foi possível consultar a compra") {
            compraSelecionada = repositorio.buscarPorId(id)
        }
    }

    fun editar() {
        limparErro()
        exibindoFormulario = true
    }

    fun voltarDoFormulario() {
        if (isSalvando) return
        limparErro()
        exibindoFormulario = false
    }

    fun voltarParaListagem() {
        if (isSalvando) return
        fecharCompra()
        carregar()
    }

    private fun fecharCompra() {
        compraSelecionada = null
        limparErro()
        exibindoFormulario = false
    }

    fun limparErro() {
        mensagemErro = null
    }

    fun gravar(
        dados: CompraFornecedorCadastroData,
        onSuccess: (String) -> Unit,
    ) {
        val atual = compraSelecionada
        if (atual?.dataEntregaPrevista != null && dados.dataEntregaPrevista == null) {
            mensagemErro = "Na edição, a data prevista pode ser substituída por outra data."
            return
        }
        if (atual?.valorFrete != null && dados.valorFrete == null) {
            mensagemErro = "Informe 0,00 para zerar o valor do frete."
            return
        }

        executar("Não foi possível gravar a compra") {
            if (atual == null) {
                repositorio.salvar(dados.toCreateRequest())
            } else {
                repositorio.atualizar(atual.id, dados.toUpdateRequest(atual))
            }
            fecharCompra()
            carregar()
            onSuccess(
                if (atual == null) {
                    "Compra cadastrada com sucesso."
                } else {
                    "Compra atualizada com sucesso."
                },
            )
        }
    }

    fun excluir(
        id: Long,
        onSuccess: (String) -> Unit,
    ) {
        executar("Não foi possível excluir a compra") {
            repositorio.excluir(id)
            carregar()
            onSuccess("Compra excluída com sucesso.")
        }
    }

    fun atualizarStatus(
        status: StatusCompraCadastro,
        onSuccess: (StatusCompraCadastro) -> Unit,
    ) {
        val compra = compraSelecionada ?: return
        executar("Não foi possível atualizar o status da compra") {
            val atualizada = repositorio.atualizarStatus(compra.id, status)
            fecharCompra()
            carregar()
            onSuccess(atualizada.status)
        }
    }

    private fun executar(
        erroPadrao: String,
        acao: suspend () -> Unit,
    ) {
        if (isSalvando || isCarregando) return
        isSalvando = true
        limparErro()
        viewModelScope.launch {
            try {
                acao()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                mensagemErro = mensagemDeErro(exception, erroPadrao)
            } finally {
                isSalvando = false
            }
        }
    }
}

internal fun CompraFornecedorCadastroData.toCreateRequest() =
    CompraFornecedorCreateRequest(
        funcionarioId = funcionarioId,
        fornecedorId = fornecedorId,
        formaPagamentoId = formaPagamentoId,
        dataCompra = dataCompra?.let { "${it}T00:00:00Z" },
        valorFrete = valorFrete?.toDouble(),
        freteIncluso = freteIncluso,
        dataEntregaPrevista = dataEntregaPrevista,
        observacao = observacao,
        itens = itens.map(CompraItemCadastroData::toRequest),
    )

internal fun CompraFornecedorCadastroData.toUpdateRequest(atual: CompraFornecedor): CompraFornecedorUpdateRequest {
    val request = toCreateRequest()
    val itensAtuais = atual.itens.map { CompraItemRequest(it.produtoId, it.quantidade, it.valorUnitario) }
    return CompraFornecedorUpdateRequest(
        funcionarioId = funcionarioId.takeUnless { it == atual.funcionarioId },
        fornecedorId = fornecedorId.takeUnless { it == atual.fornecedorId },
        formaPagamentoId = formaPagamentoId.takeUnless { it == atual.formaPagamentoId },
        dataCompra = request.dataCompra.takeUnless { dataCompra == atual.dataCompra.substringBefore('T') },
        valorFrete = request.valorFrete.takeUnless { it == atual.valorFrete },
        freteIncluso = freteIncluso,
        dataEntregaPrevista = dataEntregaPrevista.takeUnless { it == atual.dataEntregaPrevista },
        observacao = observacao.orEmpty().takeUnless { it == atual.observacao.orEmpty() },
        itens = request.itens.takeUnless { it == itensAtuais },
    )
}

private fun CompraItemCadastroData.toRequest() =
    CompraItemRequest(
        produtoId = produtoId,
        quantidade = quantidade,
        valorUnitario = valorUnitario.toDouble(),
    )
