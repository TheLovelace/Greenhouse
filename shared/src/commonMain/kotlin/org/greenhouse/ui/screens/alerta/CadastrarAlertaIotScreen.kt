package org.greenhouse.ui.screens.alerta

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.greenhouse.ui.screens.especie.CampoFormularioGreenhouse
import org.greenhouse.ui.theme.AppTypography
import org.greenhouse.ui.theme.Background
import org.greenhouse.ui.theme.GreenPrimary
import org.greenhouse.ui.theme.RedPrimary
import org.greenhouse.ui.theme.Surface
import org.greenhouse.ui.theme.TextPrimary

@Composable
fun CadastrarAlertaIotScreen(
    onBackClick: () -> Unit = {},
    onSalvarClick: (
        Long,
        Long,
        String,
        String?,
        Int,
        Boolean,
        Boolean,
        Boolean
    ) -> Unit = { _, _, _, _, _, _, _, _ -> },
) {
    var dispositivoIotId by remember { mutableStateOf("") }
    var especieId by remember { mutableStateOf("") }
    var titulo by remember { mutableStateOf("") }
    var mensagem by remember { mutableStateOf("") }
    var prioridade by remember { mutableStateOf("2") }

    var monitorarTemperatura by remember { mutableStateOf(true) }
    var monitorarUmidadeAr by remember { mutableStateOf(false) }
    var monitorarUmidadeSolo by remember { mutableStateOf(false) }

    var erro by remember { mutableStateOf("") }
    var dadosGravados by remember { mutableStateOf<DadosAlerta?>(null) }

    val typography = AppTypography()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(scrollState)
            .padding(horizontal = 30.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onBackClick) {
                Text(
                    text = "← Voltar",
                    color = GreenPrimary,
                    style = typography.titleMedium,
                )
            }
        }

        Text(
            text = "Cadastrar Alerta IoT",
            style = typography.headlineMedium,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
        )

        Text(
            text = "Preencha os dados para cadastrar uma regra de alerta.",
            style = typography.bodyLarge,
            color = TextPrimary,
        )

        CampoFormularioGreenhouse(
            label = "ID do dispositivo IoT *",
            value = dispositivoIotId,
            onValueChange = {
                dispositivoIotId = it
                erro = ""
            },
            placeholder = "Ex.: 1",
            keyboardType = KeyboardType.Number,
            typography = typography,
        )

        CampoFormularioGreenhouse(
            label = "ID da espécie *",
            value = especieId,
            onValueChange = {
                especieId = it
                erro = ""
            },
            placeholder = "Ex.: 1",
            keyboardType = KeyboardType.Number,
            typography = typography,
        )

        CampoFormularioGreenhouse(
            label = "Título do alerta *",
            value = titulo,
            onValueChange = {
                titulo = it
                erro = ""
            },
            placeholder = "Ex.: Temperatura fora do ideal",
            typography = typography,
        )

        CampoFormularioGreenhouse(
            label = "Mensagem",
            value = mensagem,
            onValueChange = { mensagem = it },
            placeholder = "Orientação para verificar a planta...",
            singleLine = false,
            typography = typography,
        )

        CampoFormularioGreenhouse(
            label = "Prioridade (1 = alta, 2 = média, 3 = baixa) *",
            value = prioridade,
            onValueChange = {
                prioridade = it
                erro = ""
            },
            placeholder = "2",
            keyboardType = KeyboardType.Number,
            typography = typography,
        )

        Text(
            text = "Condições monitoradas *",
            style = typography.titleSmall,
            color = TextPrimary,
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            LinhaCondicaoAlerta(
                texto = "Temperatura",
                selecionado = monitorarTemperatura,
                onSelecionar = {
                    monitorarTemperatura = it
                    erro = ""
                },
                typography = typography,
            )

            LinhaCondicaoAlerta(
                texto = "Umidade do ar",
                selecionado = monitorarUmidadeAr,
                onSelecionar = {
                    monitorarUmidadeAr = it
                    erro = ""
                },
                typography = typography,
            )

            LinhaCondicaoAlerta(
                texto = "Umidade do solo",
                selecionado = monitorarUmidadeSolo,
                onSelecionar = {
                    monitorarUmidadeSolo = it
                    erro = ""
                },
                typography = typography,
            )
        }

        if (erro.isNotBlank()) {
            Text(
                text = erro,
                style = typography.bodyMedium,
                color = RedPrimary,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = {
                    val dispositivoIdValido =
                        dispositivoIotId.toLongOrNull()

                    val especieIdValido =
                        especieId.toLongOrNull()

                    val prioridadeValida =
                        prioridade.toIntOrNull()

                    erro = when {
                        dispositivoIdValido == null ||
                            dispositivoIdValido <= 0L ->
                            "Informe um ID de dispositivo IoT válido."

                        especieIdValido == null ||
                            especieIdValido <= 0L ->
                            "Informe um ID de espécie válido."

                        titulo.isBlank() ->
                            "Informe o título do alerta."

                        prioridadeValida == null ||
                            prioridadeValida !in 1..3 ->
                            "A prioridade deve ser 1, 2 ou 3."

                        !monitorarTemperatura &&
                            !monitorarUmidadeAr &&
                            !monitorarUmidadeSolo ->
                            "Selecione pelo menos uma condição."

                        else -> ""
                    }

                    if (erro.isBlank()) {
                        dadosGravados = DadosAlerta(
                            dispositivoIotId = dispositivoIdValido!!,
                            especieId = especieIdValido!!,
                            titulo = titulo.trim(),
                            mensagem = mensagem.trim().ifBlank { null },
                            prioridade = prioridadeValida!!,
                            monitorarTemperatura = monitorarTemperatura,
                            monitorarUmidadeAr = monitorarUmidadeAr,
                            monitorarUmidadeSolo = monitorarUmidadeSolo,
                        )
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenPrimary,
                    contentColor = Surface,
                ),
                contentPadding = PaddingValues(horizontal = 12.dp),
            ) {
                Text(
                    text = "Gravar",
                    style = typography.titleMedium,
                )
            }

            OutlinedButton(
                onClick = {
                    dispositivoIotId = ""
                    especieId = ""
                    titulo = ""
                    mensagem = ""
                    prioridade = "2"

                    monitorarTemperatura = true
                    monitorarUmidadeAr = false
                    monitorarUmidadeSolo = false

                    erro = ""
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, GreenPrimary),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextPrimary,
                ),
                contentPadding = PaddingValues(horizontal = 12.dp),
            ) {
                Text(
                    text = "Limpar campos",
                    style = typography.titleMedium,
                )
            }
        }
    }

    dadosGravados?.let { dados ->
        val fechar = {
            dadosGravados = null
            onSalvarClick(
                dados.dispositivoIotId,
                dados.especieId,
                dados.titulo,
                dados.mensagem,
                dados.prioridade,
                dados.monitorarTemperatura,
                dados.monitorarUmidadeAr,
                dados.monitorarUmidadeSolo,
            )
        }

        AlertDialog(
            onDismissRequest = fechar,
            containerColor = Background,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "Cadastro concluído",
                    style = typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
            },
            text = {
                Text(
                    text = "Regra de alerta cadastrada com sucesso!",
                    style = typography.bodyLarge,
                    color = TextPrimary,
                )
            },
            confirmButton = {
                Button(
                    onClick = fechar,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenPrimary,
                        contentColor = Surface,
                    ),
                ) {
                    Text(
                        text = "OK",
                        style = typography.titleMedium,
                    )
                }
            },
        )
    }
}

private data class DadosAlerta(
    val dispositivoIotId: Long,
    val especieId: Long,
    val titulo: String,
    val mensagem: String?,
    val prioridade: Int,
    val monitorarTemperatura: Boolean,
    val monitorarUmidadeAr: Boolean,
    val monitorarUmidadeSolo: Boolean,
)

@Composable
private fun LinhaCondicaoAlerta(
    texto: String,
    selecionado: Boolean,
    onSelecionar: (Boolean) -> Unit,
    typography: androidx.compose.material3.Typography,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = selecionado,
            onCheckedChange = onSelecionar,
        )

        Text(
            text = texto,
            color = TextPrimary,
            style = typography.bodyMedium,
        )
    }
}