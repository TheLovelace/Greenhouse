package org.greenhouse.repository

import org.greenhouse.model.Usuario
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface UsuarioRepository : JpaRepository<Usuario, Long> {
    @Query("SELECT u FROM Usuario u WHERE u.login = :login")
    fun buscarPorLogin(@Param("login") login: String): Usuario?

    @Query(
        value = """
            SELECT u.*
            FROM usuario u
            INNER JOIN funcionario f ON f.id = u.funcionario_id
            WHERE f.cpf = :cpf AND f.email = :email
        """,
        nativeQuery = true,
    )
    fun buscarPorCpfEEmail(
        @Param("cpf") cpf: String,
        @Param("email") email: String,
    ): Usuario?

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE Usuario u SET u.senhaHash = :senhaHash WHERE u.id = :id")
    fun atualizarSenhaQuery(
        @Param("id") usuarioId: Long,
        @Param("senhaHash") novaSenhaHash: String,
    ): Int

    fun atualizarSenha(usuarioId: Long, novaSenhaHash: String): Boolean =
        atualizarSenhaQuery(usuarioId, novaSenhaHash) > 0

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(
        """
        UPDATE Usuario u
        SET u.senhaHash = :senhaHash, u.primeiroAcessoFeito = true
        WHERE u.id = :id AND u.primeiroAcessoFeito = false
        """,
    )
    fun concluirPrimeiroAcessoQuery(
        @Param("id") usuarioId: Long,
        @Param("senhaHash") novaSenhaHash: String,
    ): Int

    fun concluirPrimeiroAcesso(usuarioId: Long, novaSenhaHash: String): Boolean =
        concluirPrimeiroAcessoQuery(usuarioId, novaSenhaHash) > 0

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE Usuario u SET u.ativo = :ativo WHERE u.funcionarioId = :funcionarioId")
    fun atualizarAtivoPorFuncionario(
        @Param("funcionarioId") funcionarioId: Long,
        @Param("ativo") ativo: Boolean,
    ): Int
}
