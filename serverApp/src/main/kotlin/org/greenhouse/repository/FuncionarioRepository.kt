package org.greenhouse.repository

import org.greenhouse.model.Funcionario
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional
import jakarta.persistence.LockModeType
import java.time.LocalDate

interface FuncionarioRepository : JpaRepository<Funcionario, Long> {
    @Query("SELECT f FROM Funcionario f")
    fun listar(): List<Funcionario>

    @Query("SELECT f FROM Funcionario f WHERE f.nome LIKE CONCAT('%', :nome, '%')")
    fun buscarPorNome(@Param("nome") nome: String): List<Funcionario>

    @Query("SELECT f FROM Funcionario f WHERE f.id = :id")
    fun buscarPorId(@Param("id") id: Long): Funcionario?

    fun criar(funcionario: Funcionario): Funcionario = save(funcionario)

    @Transactional
    fun atualizar(id: Long, funcionario: Funcionario): Funcionario? {
        if (atualizarCampos(
                id,
                funcionario.nome,
                funcionario.cpf,
                funcionario.telefone,
                funcionario.email,
                funcionario.cargo,
                funcionario.ativo,
                funcionario.dataDeNascimento,
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
        UPDATE Funcionario f
        SET f.nome = :nome, f.cpf = :cpf, f.telefone = :telefone,
            f.email = :email, f.cargo = :cargo, f.ativo = :ativo,
            f.dataDeNascimento = :dataDeNascimento
        WHERE f.id = :id
        """,
    )
    fun atualizarCampos(
        @Param("id") id: Long,
        @Param("nome") nome: String,
        @Param("cpf") cpf: String,
        @Param("telefone") telefone: String?,
        @Param("email") email: String?,
        @Param("cargo") cargo: String?,
        @Param("ativo") ativo: Boolean,
        @Param("dataDeNascimento") dataDeNascimento: LocalDate?,
    ): Int

    @Modifying
    @Transactional
    @Query("DELETE FROM Funcionario f WHERE f.id = :id")
    fun excluirPorId(@Param("id") id: Long): Int

    fun excluir(id: Long): Boolean = excluirPorId(id) > 0
}
