package org.greenhouse.iot

fun interface FilaLeituraIot {
    fun assinar(topico: String, aoReceber: (payload: String) -> Unit)
}
