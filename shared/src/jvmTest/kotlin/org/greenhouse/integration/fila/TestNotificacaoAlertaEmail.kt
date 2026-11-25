package org.greenhouse.integration.fila

import java.math.BigDecimal
import java.time.OffsetDateTime
import org.greenhouse.adapter.GlobalExceptionHandler
import org.greenhouse.adapter.NotificacaoAlertaAdapter
import org.greenhouse.dto.AvaliacaoLeituraDTO
import org.greenhouse.dto.OcorrenciaAlertaDTO
import org.greenhouse.enums.CondicaoAmbiental
import org.greenhouse.enums.ParametroAmbiental
import org.greenhouse.model.DispositivoIot
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoInteractions
import org.springframework.mail.javamail.JavaMailSenderImpl

/**
 * Teste do envio real do e-mail de alerta ao admin, pelo SMTP configurado no `.env.test.local`
 * (MAIL_HOST, MAIL_PORT, MAIL_USERNAME, MAIL_PASSWORD, ALERTA_EMAIL_REMETENTE e
 * ALERTA_EMAIL_DESTINO). Roda só pela task `testFila`, fora do `jvmTest` e da pipeline.
 *
 * Cada execução envia um e-mail de verdade para ALERTA_EMAIL_DESTINO, com "[TESTE]" no nome do
 * dispositivo para diferenciar de um alerta real.
 */
class TestNotificacaoAlertaEmail {
    private fun variavel(nome: String): String =
            requireNotNull(System.getenv(nome)?.takeIf { it.isNotBlank() }) {
                "A variável $nome é necessária para o teste de envio de e-mail"
            }

    private fun mailSender() =
            JavaMailSenderImpl().apply {
                host = variavel("MAIL_HOST")
                port = System.getenv("MAIL_PORT")?.toIntOrNull() ?: 587
                username = variavel("MAIL_USERNAME")
                password = variavel("MAIL_PASSWORD")
                javaMailProperties["mail.smtp.auth"] = "true"
                javaMailProperties["mail.smtp.starttls.enable"] = "true"
                javaMailProperties["mail.smtp.connectiontimeout"] = "10000"
                javaMailProperties["mail.smtp.timeout"] = "10000"
            }

    private val dispositivoDeTeste =
            DispositivoIot(
                    id = 999L,
                    codigo = "GH-TESTE-FILA",
                    nome = "[TESTE] Estufa da task testFila",
                    localizacao = "Teste automatizado",
                    topicoMqtt = "greenhouse/teste/sensor",
                    calibracaoSeco = 3000,
                    calibracaoUmido = 1000,
            )

    private val leituraForaDaFaixa =
            AvaliacaoLeituraDTO(
                    leituraId = null,
                    dispositivoId = 999L,
                    temperatura = BigDecimal("34.50"),
                    umidadeAr = BigDecimal("66.00"),
                    umidadeSolo = BigDecimal("65.00"),
                    dataLeitura = OffsetDateTime.now(),
                    ocorrencias =
                            listOf(
                                    OcorrenciaAlertaDTO(
                                            alertaId = null,
                                            titulo = "[TESTE] Alerta temperatura alta",
                                            prioridade = 2,
                                            especieId = null,
                                            parametro = ParametroAmbiental.TEMPERATURA,
                                            valorMedido = BigDecimal("34.50"),
                                            condicao = CondicaoAmbiental.ACIMA_DO_IDEAL,
                                    ),
                            ),
            )

    // CT01 - Fluxo principal: o adapter envia pelo SMTP real o e-mail de alerta ao admin sem
    // registrar nenhuma falha no GlobalExceptionHandler
    @Test
    fun `deve enviar e-mail de alerta ao admin pelo SMTP configurado`() {
        val exceptionHandler = mock(GlobalExceptionHandler::class.java)
        val adapter =
                NotificacaoAlertaAdapter(
                        mailSender(),
                        exceptionHandler,
                        System.getenv("ALERTA_EMAIL_REMETENTE").orEmpty(),
                        variavel("ALERTA_EMAIL_DESTINO"),
                )

        assertDoesNotThrow { adapter.notificar(dispositivoDeTeste, leituraForaDaFaixa) }

        // O adapter não propaga falhas de envio: elas iriam para o handler. Sem interações,
        // o SMTP aceitou a mensagem.
        verifyNoInteractions(exceptionHandler)
    }
}
