package org.greenhouse.enums

import java.math.BigDecimal

enum class NivelComplexidade(val precoMinimo: BigDecimal) {
    BAIXA(BigDecimal.ZERO),
    MEDIA(BigDecimal("30.00")),
    ALTA(BigDecimal("80.00")),
}
