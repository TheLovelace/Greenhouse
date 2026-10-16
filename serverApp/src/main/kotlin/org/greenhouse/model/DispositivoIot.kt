package org.greenhouse.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "dispositivo_iot")
data class DispositivoIot(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @field:Column(
        name = "codigo",
        nullable = false,
        unique = true,
        length = 50
    )
    var codigo: String = "",

    @field:Column(
        name = "nome",
        nullable = false,
        length = 150
    )
    var nome: String = "",

    @field:Column(name = "localizacao", length = 200)
    var localizacao: String? = null,

    @field:Column(
        name = "topico_mqtt",
        nullable = false,
        unique = true,
        length = 255
    )
    var topicoMqtt: String = "",

    @field:Column(name = "calibracao_seco")
    var calibracaoSeco: Int? = null,

    @field:Column(name = "calibracao_umido")
    var calibracaoUmido: Int? = null,

    @field:Column(name = "ativo", nullable = false)
    var ativo: Boolean = true,
)