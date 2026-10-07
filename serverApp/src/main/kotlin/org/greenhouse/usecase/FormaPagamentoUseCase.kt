package org.greenhouse.usecase

import org.greenhouse.adapter.BadRequestException
import org.greenhouse.adapter.NotFoundException
import org.greenhouse.adapter.UnprocessableEntityException
import org.greenhouse.entity.FormaPagamento
import org.greenhouse.repository.FormaPagamentoRepository
import org.springframework.stereotype.Service

@Service
class FormaPagamentoUseCase(
    private val formaPagamentoRepository: FormaPagamentoRepository,
) {
    fun criar(formaPagamento: FormaPagamento): FormaPagamento {
        validar(formaPagamento)
        return formaPagamentoRepository.criar(formaPagamento.copy(id = null))
    }

    fun listar(): List<FormaPagamento> = formaPagamentoRepository.listar()

    fun buscarPorId(id: Long): FormaPagamento =
        formaPagamentoRepository.buscarPorId(validarId(id))
            ?: throw NotFoundException("Forma de pagamento não encontrada")

    fun atualizar(
        id: Long,
        campos: Map<String, Any?>,
    ): FormaPagamento {
        val idValidado = validarId(id)
        val atual =
            formaPagamentoRepository
                .buscarPorId(idValidado)
                ?: throw NotFoundException("Forma de pagamento não encontrada")

        val camposPermitidos = setOf("id", "nome", "ativo")
        val camposDesconhecidos = campos.keys - camposPermitidos
        if (camposDesconhecidos.isNotEmpty()) {
            throw BadRequestException("Campo(s) não reconhecido(s): ${camposDesconhecidos.joinToString()}")
        }
        if (campos.keys.none { it != "id" }) {
            throw BadRequestException("Informe ao menos um campo para atualizar")
        }

        val atualizada =
            atual.copy(
                nome = campos.texto("nome", atual.nome),
                ativo = campos.booleano("ativo", atual.ativo),
            )
        validar(atualizada)

        return formaPagamentoRepository.atualizar(idValidado, atualizada)
            ?: throw NotFoundException("Forma de pagamento não encontrada")
    }

    fun excluir(id: Long) {
        if (!formaPagamentoRepository.inativar(validarId(id))) {
            throw NotFoundException("Forma de pagamento não encontrada")
        }
    }

    private fun validar(formaPagamento: FormaPagamento) {
        if (formaPagamento.nome.isBlank()) {
            throw UnprocessableEntityException("O nome da forma de pagamento é obrigatório")
        }
        if (formaPagamento.nome.length > 50) {
            throw UnprocessableEntityException("O nome da forma de pagamento deve ter no máximo 50 caracteres")
        }
    }

    private fun validarId(id: Long): Long {
        if (id <= 0) {
            throw BadRequestException("O id da forma de pagamento deve ser maior que zero")
        }
        return id
    }

    private fun Map<String, Any?>.texto(
        campo: String,
        valorAtual: String,
    ): String {
        if (!containsKey(campo)) return valorAtual
        return this[campo] as? String
            ?: throw BadRequestException("O campo '$campo' deve ser texto")
    }

    private fun Map<String, Any?>.booleano(
        campo: String,
        valorAtual: Boolean,
    ): Boolean {
        if (!containsKey(campo)) return valorAtual
        return this[campo] as? Boolean
            ?: throw BadRequestException("O campo '$campo' deve ser booleano")
    }
}
