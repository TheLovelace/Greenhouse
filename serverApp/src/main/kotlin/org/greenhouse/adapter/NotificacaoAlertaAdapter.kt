package org.greenhouse.adapter

import java.time.format.DateTimeFormatter
import org.greenhouse.dto.AvaliacaoLeituraDTO
import org.greenhouse.enums.CondicaoAmbiental
import org.greenhouse.enums.ParametroAmbiental
import org.greenhouse.model.DispositivoIot
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Component

/**
 * Adapter de saída (SMTP) do RF17: avisa o admin por e-mail quando uma leitura IoT fica fora da faixa ideal de uma espécie
 * monitorada. Só é criado quando o SMTP (`MAIL_HOST`) e o destinatário (`ALERTA_EMAIL_DESTINO`)
 * estão configurados. Falhas de envio são registradas pelo [GlobalExceptionHandler] e não
 * impedem o armazenamento da leitura.
 */
@Component
@ConditionalOnExpression(
        "!'\${spring.mail.host:}'.isBlank() and !'\${alerta.email.destino:}'.isBlank()"
)
class NotificacaoAlertaAdapter(
        private val mailSender: JavaMailSender,
        private val exceptionHandler: GlobalExceptionHandler,
        @Value("\${alerta.email.remetente}") private val remetente: String,
        @Value("\${alerta.email.destino}") private val destino: String,
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")

    fun notificar(dispositivo: DispositivoIot, avaliacao: AvaliacaoLeituraDTO) {
        try {
            val mensagem =
                    SimpleMailMessage().apply {
                        from = remetente.ifBlank { destino }
                        setTo(destino)
                        subject = assunto(dispositivo, avaliacao)
                        text = corpo(dispositivo, avaliacao)
                    }
            mailSender.send(mensagem)
            log.info("E-mail de alerta enviado para o dispositivo {}", dispositivo.codigo)
        } catch (e: Exception) {
            exceptionHandler.registrar(
                    ServerException(
                            "Falha ao enviar e-mail de alerta do dispositivo ${dispositivo.codigo}",
                            e,
                    ),
                    "Notificação de alerta",
            )
        }
    }

    private fun assunto(dispositivo: DispositivoIot, avaliacao: AvaliacaoLeituraDTO): String {
        val maiorPrioridade = avaliacao.ocorrencias.maxOf { it.prioridade }
        return "[Greenhouse] Alerta (prioridade $maiorPrioridade) - ${dispositivo.nome}"
    }

    private fun corpo(dispositivo: DispositivoIot, avaliacao: AvaliacaoLeituraDTO): String = buildString {
        appendLine("Uma leitura do dispositivo IoT ficou fora da faixa ideal.")
        appendLine()
        appendLine("Dispositivo: ${dispositivo.nome} (${dispositivo.codigo})")
        dispositivo.localizacao?.let { appendLine("Localização: $it") }
        appendLine("Data da leitura: ${avaliacao.dataLeitura.format(formatoData)}")
        appendLine()
        appendLine("Valores medidos:")
        appendLine("  - Temperatura: ${avaliacao.temperatura} °C")
        appendLine("  - Umidade do ar: ${avaliacao.umidadeAr} %")
        appendLine("  - Umidade do solo: ${avaliacao.umidadeSolo} %")
        appendLine()
        appendLine("Ocorrências:")
        avaliacao.ocorrencias.sortedByDescending { it.prioridade }.forEach {
            appendLine(
                    "  - ${it.titulo} (prioridade ${it.prioridade}): " +
                            "${nomeParametro(it.parametro)} = ${it.valorMedido} " +
                            "está ${descricaoCondicao(it.condicao)}"
            )
        }
    }

    private fun nomeParametro(parametro: ParametroAmbiental) =
            when (parametro) {
                ParametroAmbiental.TEMPERATURA -> "Temperatura"
                ParametroAmbiental.UMIDADE_AR -> "Umidade do ar"
                ParametroAmbiental.UMIDADE_SOLO -> "Umidade do solo"
            }

    private fun descricaoCondicao(condicao: CondicaoAmbiental) =
            when (condicao) {
                CondicaoAmbiental.ABAIXO_DO_IDEAL -> "abaixo do ideal"
                CondicaoAmbiental.ACIMA_DO_IDEAL -> "acima do ideal"
                else -> condicao.name.lowercase()
            }
}
