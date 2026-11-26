@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.screens.ajuda

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import greenhouse.shared.generated.resources.Res
import greenhouse.shared.generated.resources.icon_voltar
import org.greenhouse.ui.theme.AppTypography
import org.greenhouse.ui.theme.Background
import org.greenhouse.ui.theme.GreenPrimary
import org.greenhouse.ui.theme.Surface
import org.greenhouse.ui.theme.TextPrimary
import org.jetbrains.compose.resources.painterResource

private data class PerguntaFrequente(
    val pergunta: String,
    val resposta: String,
)

private val perguntasFrequentes =
    listOf(
        PerguntaFrequente(
            pergunta = "Como acesso uma funcionalidade?",
            resposta = "Volte à Home e selecione o card da funcionalidade desejada. Use o botão de voltar para retornar à tela anterior.",
        ),
        PerguntaFrequente(
            pergunta = "Como cadastro ou edito um item?",
            resposta = "Abra a listagem do módulo e escolha cadastrar ou editar. Preencha os campos obrigatórios e toque em Gravar.",
        ),
        PerguntaFrequente(
            pergunta = "Como faço o primeiro acesso?",
            resposta = "Na tela de login, escolha Primeiro acesso. Informe os dados solicitados para validar sua conta e definir uma senha.",
        ),
        PerguntaFrequente(
            pergunta = "O que significam os perfis exibidos no header?",
            resposta = "Administrador identifica uma conta com perfil ADMIN. Funcionário identifica uma conta com perfil FUNCIONARIO.",
        ),
        PerguntaFrequente(
            pergunta = "O que fazer se o sistema não conectar à API?",
            resposta = "Confira se o backend está em execução e se o frontend aponta para a URL e porta corretas. No ambiente local, a porta padrão é 8080; o container de teste usa 8081.",
        ),
        PerguntaFrequente(
            pergunta = "Como encerro minha sessão?",
            resposta = "Use o botão Sair no canto superior direito da Home. A sessão será removida deste dispositivo.",
        ),
    )

@Composable
fun AjudaScreen(onBack: () -> Unit) {
    val typography = AppTypography()

    Column(
        modifier = Modifier.fillMaxSize().background(Background).padding(horizontal = 24.dp, vertical = 20.dp),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val titleFontSize =
                when {
                    maxWidth < 360.dp -> 18.sp
                    maxWidth < 520.dp -> 22.sp
                    else -> typography.headlineMedium.fontSize
                }
            Box(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Button(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart).size(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Surface, contentColor = GreenPrimary),
                    contentPadding = PaddingValues(12.dp),
                ) {
                    Image(
                        painter = painterResource(Res.drawable.icon_voltar),
                        contentDescription = "Voltar para a Home",
                        modifier = Modifier.size(24.dp),
                    )
                }
                Text(
                    text = "Central de Ajuda",
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 56.dp),
                    color = TextPrimary,
                    style = typography.headlineMedium.copy(fontSize = titleFontSize),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("Perguntas frequentes", color = TextPrimary, style = typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 12.dp),
        ) {
            items(perguntasFrequentes, key = { it.pergunta }) { item ->
                PerguntaFrequenteCard(item)
            }
        }
    }
}

@Composable
private fun PerguntaFrequenteCard(item: PerguntaFrequente) {
    var expandida by remember { mutableStateOf(false) }
    val typography = AppTypography()

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .animateContentSize()
                .clickable { expandida = !expandida },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, GreenPrimary.copy(alpha = 0.35f)),
        colors = CardDefaults.cardColors(containerColor = Surface),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = item.pergunta,
                    modifier = Modifier.weight(1f),
                    color = TextPrimary,
                    style = typography.titleMedium,
                )
                Text(
                    text = if (expandida) "-" else "+",
                    color = GreenPrimary,
                    style = typography.titleLarge,
                )
            }
            AnimatedVisibility(visible = expandida) {
                Text(
                    text = item.resposta,
                    modifier = Modifier.padding(top = 12.dp),
                    color = TextPrimary,
                    style = typography.bodyMedium,
                )
            }
        }
    }
}
