package org.greenhouse.unit.adapter

import java.math.BigDecimal
import java.time.OffsetDateTime
import java.time.ZoneOffset
import org.greenhouse.dto.AvaliacaoLeituraDTO
import org.greenhouse.dto.OcorrenciaAlertaDTO
import org.greenhouse.enums.CondicaoAmbiental
import org.greenhouse.enums.ParametroAmbiental
import org.greenhouse.adapter.AppException
import org.greenhouse.adapter.GlobalExceptionHandler
import org.greenhouse.adapter.NotificacaoAlertaAdapter
import org.greenhouse.adapter.ServerException
import org.greenhouse.unit.fixture.MonitoramentoFixture.dispositivo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.anyString
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.mail.MailSendException
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender

/**
 * Testes de unidade do adapter NotificacaoAlertaAdapter (RF17 - monitoramento por IoT).
 *
 * Recursos de teste usados:
 * - MOCK [mailSender]: substitui o servidor SMTP; a mensagem enviada é capturada para conferir
 * remetente, destinatário, assunto e corpo.
 * - MOCK [exceptionHandler]: GlobalExceptionHandler que registra as falhas de envio.
 * - FIXTURE: dispositivo de MonitoramentoFixture.
 *
 * Regras de negócio cobertas:
 * - RN04: Situações fora dos limites estabelecidos devem ficar identificadas para possível geração
 * de alertas.
 */
@ExtendWith(MockitoExtension::class)
class NotificacaoAlertaAdapterTest {
    @Mock lateinit var mailSender: JavaMailSender

    @Mock lateinit var exceptionHandler: GlobalExceptionHandler

    private val remetente = "greenhouse@exemplo.com"
    private val destino = "admin@exemplo.com"

    private fun adapter() = NotificacaoAlertaAdapter(mailSender, exceptionHandler, remetente, destino)

    private fun avaliacaoComOcorrencias() =
            AvaliacaoLeituraDTO(
                    leituraId = 500L,
                    dispositivoId = 10L,
                    temperatura = BigDecimal("31.00"),
                    umidadeAr = BigDecimal("40.00"),
                    umidadeSolo = BigDecimal("55.00"),
                    dataLeitura = OffsetDateTime.of(2026, 11, 25, 14, 30, 0, 0, ZoneOffset.UTC),
                    ocorrencias =
                            listOf(
                                    OcorrenciaAlertaDTO(
                                            alertaId = 100L,
                                            titulo = "Umidade baixa",
                                            prioridade = 2,
                                            especieId = 1L,
                                            parametro = ParametroAmbiental.UMIDADE_AR,
                                            valorMedido = BigDecimal("40.00"),
                                            condicao = CondicaoAmbiental.ABAIXO_DO_IDEAL,
                                    ),
                                    OcorrenciaAlertaDTO(
                                            alertaId = 101L,
                                            titulo = "Calor excessivo",
                                            prioridade = 3,
                                            especieId = 1L,
                                            parametro = ParametroAmbiental.TEMPERATURA,
                                            valorMedido = BigDecimal("31.00"),
                                            condicao = CondicaoAmbiental.ACIMA_DO_IDEAL,
                                    ),
                            ),
            )

    // RN04: Situações fora dos limites estabelecidos devem ficar identificadas para possível
    // geração de alertas.
    // CT01 - Fluxo principal: envia ao admin um e-mail com o dispositivo, os valores e as ocorrências
    @Test
    fun `deve enviar e-mail ao admin com as ocorrencias da leitura`() {
        val dispositivoDaEstufa = dispositivo()

        adapter().notificar(dispositivoDaEstufa, avaliacaoComOcorrencias())

        val captor = ArgumentCaptor.forClass(SimpleMailMessage::class.java)
        verify(mailSender).send(captor.capture())
        val email = captor.value
        val corpo = email.text.orEmpty()
        assertAll(
                "e-mail de alerta",
                { assertEquals(remetente, email.from, "remetente configurado") },
                { assertEquals(listOf(destino), email.to?.toList(), "destinatário é o admin") },
                {
                    assertTrue(
                            email.subject.orEmpty().contains("prioridade 3"),
                            "o assunto destaca a maior prioridade"
                    )
                },
                {
                    assertTrue(
                            email.subject.orEmpty().contains(dispositivoDaEstufa.nome),
                            "o assunto identifica o dispositivo"
                    )
                },
                {
                    assertTrue(
                            corpo.contains("Calor excessivo (prioridade 3): Temperatura = 31.00 está acima do ideal"),
                            "descreve a ocorrência de temperatura"
                    )
                },
                {
                    assertTrue(
                            corpo.contains("Umidade baixa (prioridade 2): Umidade do ar = 40.00 está abaixo do ideal"),
                            "descreve a ocorrência de umidade do ar"
                    )
                },
                {
                    assertTrue(
                            corpo.indexOf("Calor excessivo") < corpo.indexOf("Umidade baixa"),
                            "ocorrências ordenadas da maior para a menor prioridade"
                    )
                },
        )
    }

    // RN04: Situações fora dos limites estabelecidos devem ficar identificadas para possível
    // geração de alertas.
    // CT02 - Fluxo de exceção: falha no SMTP não propaga (para não impedir o registro da leitura) e
    // é registrada pelo GlobalExceptionHandler
    @Test
    fun `nao deve propagar falha no envio do e-mail`() {
        doThrow(MailSendException("SMTP indisponível"))
                .`when`(mailSender)
                .send(any(SimpleMailMessage::class.java))

        assertDoesNotThrow("uma falha no e-mail não pode derrubar o processamento da leitura") {
            adapter().notificar(dispositivo(), avaliacaoComOcorrencias())
        }

        val erroRegistrado = ArgumentCaptor.forClass(AppException::class.java)
        verify(exceptionHandler)
                .registrar(erroRegistrado.capture() ?: ServerException(), anyString() ?: "")
        assertAll(
                "falha registrada pelo GlobalExceptionHandler",
                {
                    assertTrue(
                            erroRegistrado.value is ServerException,
                            "falha no SMTP é registrada como ServerException"
                    )
                },
                {
                    assertTrue(
                            erroRegistrado.value.cause is MailSendException,
                            "mantém a exceção original do SMTP como causa"
                    )
                },
        )
    }
}
