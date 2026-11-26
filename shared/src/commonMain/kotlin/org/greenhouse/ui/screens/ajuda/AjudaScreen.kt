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
            pergunta = "O que posso fazer no Greenhouse?",
            resposta = "O Greenhouse ajuda a organizar a rotina da floricultura. Pela Home, você acessa áreas como produtos, estoque, descontos, pedidos, clientes e funcionários.",
        ),
        PerguntaFrequente(
            pergunta = "Como encontro a área que preciso?",
            resposta = "Na Home, escolha o card com o nome da área desejada. Dentro das listagens, use a busca e os filtros disponíveis para localizar registros. Para voltar, use a seta no topo da tela.",
        ),
        PerguntaFrequente(
            pergunta = "Em quais dispositivos posso usar o Greenhouse?",
            resposta = "Você pode acessar o Greenhouse em dispositivos Android, iOS, computadores Desktop e pelo navegador Web.",
        ),
        PerguntaFrequente(
            pergunta = "Como cadastro ou atualizo um produto?",
            resposta = "Na Home, abra Produtos. Na listagem, escolha cadastrar ou selecione um produto para editar. Preencha os campos obrigatórios e toque em Gravar.",
        ),
        PerguntaFrequente(
            pergunta = "Como cadastro o estoque de um produto?",
            resposta = "Na Home, abra Estoque e escolha cadastrar. Informe o ID de um produto existente, as quantidades física e mínima e, se desejar, a localização. Depois toque em Gravar. O sistema calcula as quantidades reservada e disponível.",
        ),
        PerguntaFrequente(
            pergunta = "Como cadastro ou altero um desconto?",
            resposta = "Na Home, abra Descontos. Cadastre um desconto ou escolha um existente para editar. Informe o tipo, o valor e o período de validade e toque em Gravar. Ao excluir, o desconto é inativado.",
        ),
        PerguntaFrequente(
            pergunta = "Como finalizo um pedido?",
            resposta = "No momento, a área Pedidos ainda não oferece uma opção para acompanhar ou finalizar pedidos pela interface. Essa etapa precisa ser feita pelo processo atual da sua floricultura até que o gerenciamento de pedidos esteja disponível no aplicativo.",
        ),
        PerguntaFrequente(
            pergunta = "Como cadastro um novo funcionário?",
            resposta = "Somente o perfil Administrador pode cadastrar funcionários. Na Home, abra Funcionários, escolha cadastrar, preencha nome, CPF e e-mail e toque em Gravar. O funcionário poderá criar a senha pela opção Primeiro acesso na tela de login.",
        ),
        PerguntaFrequente(
            pergunta = "Como faço meu primeiro acesso?",
            resposta = "Na tela de login, escolha Primeiro acesso. Informe os dados solicitados para validar seu cadastro e criar uma senha.",
        ),
        PerguntaFrequente(
            pergunta = "Qual é a diferença entre Administrador e Funcionário?",
            resposta = "O perfil Administrador tem acesso a ações restritas, como cadastrar funcionários. Se precisar de uma ação que não aparece ou não está disponível para sua conta, fale com o administrador da floricultura.",
        ),
        PerguntaFrequente(
            pergunta = "O que faço se não conseguir entrar ou salvar?",
            resposta = "Confira sua conexão e tente entrar novamente. Se o problema continuar, peça ajuda ao administrador da floricultura.",
        ),
        PerguntaFrequente(
            pergunta = "Como saio da minha conta?",
            resposta = "Na Home, toque em Sair no canto superior direito para encerrar sua sessão neste dispositivo.",
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
