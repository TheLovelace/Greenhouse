@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.screens.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import greenhouse.shared.generated.resources.Res
import greenhouse.shared.generated.resources.welcome_background_desktop
import greenhouse.shared.generated.resources.welcome_background_mobile
import greenhouse.shared.generated.resources.welcome_card_desktop
import greenhouse.shared.generated.resources.welcome_card_mobile
import greenhouse.shared.generated.resources.welcome_divider
import org.greenhouse.ui.components.ArrowDirection
import org.greenhouse.ui.components.ArrowIcon
import org.greenhouse.ui.theme.AppTypography
import org.greenhouse.ui.theme.GreenPrimary
import org.greenhouse.ui.theme.GreenSecondary
import org.greenhouse.ui.theme.PinkPrimary
import org.greenhouse.ui.theme.TextPrimary
import org.greenhouse.ui.theme.TextSecondary
import org.jetbrains.compose.resources.painterResource

@Composable
fun WelcomeScreen(onLogin: () -> Unit) {
    val typography = AppTypography()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val desktop = maxWidth >= 700.dp
        val cardWidth =
            if (desktop) {
                (maxWidth * 0.64f).coerceAtMost(1160.dp)
            } else {
                (maxWidth - 32.dp).coerceAtMost(420.dp)
            }
        val cardHeight =
            if (desktop) {
                (maxHeight * 0.74f).coerceAtMost(820.dp)
            } else {
                (cardWidth * 1.03f).coerceAtMost(maxHeight - 32.dp)
            }
        val cardShape = RoundedCornerShape(if (desktop) 42.dp else 34.dp)

        Image(
            painter = painterResource(if (desktop) Res.drawable.welcome_background_desktop else Res.drawable.welcome_background_mobile),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(WindowInsets.safeDrawing.asPaddingValues()),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier =
                    Modifier
                        .width(cardWidth)
                        .height(cardHeight)
                        .clip(cardShape),
            ) {
                Image(
                    painter = painterResource(if (desktop) Res.drawable.welcome_card_desktop else Res.drawable.welcome_card_mobile),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        GreenSecondary.copy(alpha = 0.08f),
                                        PinkPrimary.copy(alpha = 0.08f),
                                    ),
                                ),
                            ),
                )
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = if (desktop) 72.dp else 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "Greenhouse",
                        color = GreenPrimary,
                        style = typography.displayLarge.copy(fontSize = if (desktop) 56.sp else 34.sp),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(if (desktop) 10.dp else 6.dp))
                    Text(
                        text = "Cultivando o crescimento e a harmonia do seu negócio verde",
                        color = TextPrimary,
                        style = typography.bodyLarge.copy(fontSize = if (desktop) 20.sp else 14.sp),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(0.9f),
                    )
                    Spacer(Modifier.height(if (desktop) 16.dp else 12.dp))
                    Image(
                        painter = painterResource(Res.drawable.welcome_divider),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.width(if (desktop) 240.dp else 140.dp).height(if (desktop) 30.dp else 22.dp),
                    )
                    Spacer(Modifier.height(if (desktop) 22.dp else 16.dp))
                    Button(
                        onClick = onLogin,
                        modifier = Modifier.fillMaxWidth(if (desktop) 0.52f else 0.86f).height(if (desktop) 58.dp else 46.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary, contentColor = TextSecondary),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Entrar", style = typography.titleMedium.copy(fontSize = if (desktop) 18.sp else 15.sp))
                            ArrowIcon(
                                direction = ArrowDirection.Right,
                                color = TextSecondary,
                                modifier = Modifier.width(20.dp).height(16.dp),
                            )
                        }
                    }
                    Spacer(Modifier.height(if (desktop) 20.dp else 16.dp))
                    Text(
                        text = if (desktop) "Gestão inteligente para Floriculturas" else "Gestão inteligente de floricultura.",
                        color = TextPrimary,
                        style = typography.bodyMedium.copy(fontSize = if (desktop) 16.sp else 11.sp),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (desktop) "v1.0 - Greenhouse" else "© Greenhouse",
                        color = GreenSecondary,
                        style = typography.bodySmall.copy(fontSize = if (desktop) 12.sp else 9.sp),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
