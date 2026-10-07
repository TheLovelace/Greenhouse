package org.greenhouse.repository

import org.greenhouse.model.Cliente
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface ClienteRepository : JpaRepository<Cliente, Long> {
    fun listar(
        nome: String? = null,
        telefone: String? = null,
        ativo: Boolean? = null,
    ): List<Cliente> =
        buscarFiltrados(
            filtrarNome = nome != null,
            nome = nome.orEmpty(),
            filtrarTelefone = telefone != null,
            telefone = telefone.orEmpty(),
            filtrarAtivo = ativo != null,
            ativo = ativo ?: false,
        )

    @Query(
        """
        SELECT c FROM Cliente c
        WHERE (:filtrarNome = false OR LOWER(c.nome) LIKE CONCAT('%', LOWER(:nome), '%'))
          AND (:filtrarTelefone = false OR c.telefone LIKE CONCAT('%', :telefone, '%'))
          AND (:filtrarAtivo = false OR c.ativo = :ativo)
        """,
    )
    fun buscarFiltrados(
        @Param("filtrarNome") filtrarNome: Boolean,
        @Param("nome") nome: String,
        @Param("filtrarTelefone") filtrarTelefone: Boolean,
        @Param("telefone") telefone: String,
        @Param("filtrarAtivo") filtrarAtivo: Boolean,
        @Param("ativo") ativo: Boolean,
    ): List<Cliente>

    @Query("SELECT c FROM Cliente c WHERE c.id = :id")
    fun buscarPorId(@Param("id") id: Long): Cliente?

    @Query("SELECT c FROM Cliente c WHERE c.cpf = :cpf")
    fun buscarPorCpf(@Param("cpf") cpf: String): Cliente?

    fun criar(cliente: Cliente): Cliente = save(cliente)

    @Transactional
    fun atualizar(id: Long, cliente: Cliente): Cliente? {
        if (atualizarCampos(
                id,
                cliente.nome,
                cliente.telefone,
                cliente.whatsapp,
                cliente.cpf,
                cliente.email,
                cliente.ativo,
            ) == 0
        ) {
            return null
        }
        return buscarPorId(id)
    }

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(
        """
        UPDATE Cliente c
        SET c.nome = :nome, c.telefone = :telefone, c.whatsapp = :whatsapp,
            c.cpf = :cpf, c.email = :email, c.ativo = :ativo
        WHERE c.id = :id
        """,
    )
    fun atualizarCampos(
        @Param("id") id: Long,
        @Param("nome") nome: String,
        @Param("telefone") telefone: String,
        @Param("whatsapp") whatsapp: Boolean,
        @Param("cpf") cpf: String?,
        @Param("email") email: String?,
        @Param("ativo") ativo: Boolean,
    ): Int

    fun excluir(id: Long): Boolean = excluirPorId(id) > 0

    @Modifying
    @Transactional
    @Query("DELETE FROM Cliente c WHERE c.id = :id")
    fun excluirPorId(@Param("id") id: Long): Int
}
