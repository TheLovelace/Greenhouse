package org.greenhouse.repository

import jakarta.persistence.LockModeType
import org.greenhouse.enums.ModalidadePedido
import org.greenhouse.enums.StatusPedido
import org.greenhouse.model.Pedido
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface PedidoRepository : JpaRepository<Pedido, Long> {
    fun listar(
        clienteId: Long? = null,
        funcionarioId: Long? = null,
        status: StatusPedido? = null,
        modalidade: ModalidadePedido? = null,
    ): List<Pedido> =
        buscarFiltrados(
            filtrarCliente = clienteId != null,
            clienteId = clienteId ?: 0,
            filtrarFuncionario = funcionarioId != null,
            funcionarioId = funcionarioId ?: 0,
            filtrarStatus = status != null,
            status = status ?: StatusPedido.ABERTO,
            filtrarModalidade = modalidade != null,
            modalidade = modalidade ?: ModalidadePedido.IMEDIATA,
        )

    @Query(
        """
        SELECT p FROM Pedido p
        WHERE (:filtrarCliente = false OR p.clienteId = :clienteId)
          AND (:filtrarFuncionario = false OR p.funcionarioId = :funcionarioId)
          AND (:filtrarStatus = false OR p.status = :status)
          AND (:filtrarModalidade = false OR p.modalidade = :modalidade)
        ORDER BY p.dataPedido DESC, p.id DESC
        """,
    )
    fun buscarFiltrados(
        @Param("filtrarCliente") filtrarCliente: Boolean,
        @Param("clienteId") clienteId: Long,
        @Param("filtrarFuncionario") filtrarFuncionario: Boolean,
        @Param("funcionarioId") funcionarioId: Long,
        @Param("filtrarStatus") filtrarStatus: Boolean,
        @Param("status") status: StatusPedido,
        @Param("filtrarModalidade") filtrarModalidade: Boolean,
        @Param("modalidade") modalidade: ModalidadePedido,
    ): List<Pedido>

    @Query("SELECT p FROM Pedido p WHERE p.id = :id")
    fun buscarPorId(@Param("id") id: Long): Pedido?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Pedido p WHERE p.id = :id")
    fun buscarParaAtualizar(@Param("id") id: Long): Pedido?

    fun excluir(id: Long): Boolean = excluirPorId(id) > 0

    @Modifying
    @Transactional
    @Query("DELETE FROM Pedido p WHERE p.id = :id")
    fun excluirPorId(@Param("id") id: Long): Int
}
