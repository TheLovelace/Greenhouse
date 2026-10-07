package org.greenhouse.repository

import org.greenhouse.model.FormaPagamento
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface FormaPagamentoRepository : JpaRepository<FormaPagamento, Long> {
    @Query("SELECT f FROM FormaPagamento f ORDER BY f.id")
    fun listar(): List<FormaPagamento>

    @Query("SELECT f FROM FormaPagamento f WHERE f.id = :id")
    fun buscarPorId(@Param("id") id: Long): FormaPagamento?

    fun criar(formaPagamento: FormaPagamento): FormaPagamento = save(formaPagamento)

    @Transactional
    fun atualizar(id: Long, formaPagamento: FormaPagamento): FormaPagamento? {
        if (atualizarCampos(id, formaPagamento.nome, formaPagamento.ativo) == 0) return null
        return buscarPorId(id)
    }

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE FormaPagamento f SET f.nome = :nome, f.ativo = :ativo WHERE f.id = :id")
    fun atualizarCampos(
        @Param("id") id: Long,
        @Param("nome") nome: String,
        @Param("ativo") ativo: Boolean,
    ): Int

    fun inativar(id: Long): Boolean = inativarPorId(id) > 0

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE FormaPagamento f SET f.ativo = false WHERE f.id = :id")
    fun inativarPorId(@Param("id") id: Long): Int
}
