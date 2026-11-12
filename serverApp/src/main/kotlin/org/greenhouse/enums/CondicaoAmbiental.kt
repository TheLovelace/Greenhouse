package org.greenhouse.enums

enum class CondicaoAmbiental {
    ABAIXO_DO_IDEAL,
    IDEAL,
    ACIMA_DO_IDEAL,
    NAO_CONFIGURADO,
    ;

    val foraDoLimite: Boolean
        get() = this == ABAIXO_DO_IDEAL || this == ACIMA_DO_IDEAL
}
