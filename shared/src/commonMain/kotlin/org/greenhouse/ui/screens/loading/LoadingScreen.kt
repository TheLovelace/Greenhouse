@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.screens.loading

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import greenhouse.shared.generated.resources.Res
import greenhouse.shared.generated.resources.loading_flower
import org.greenhouse.ui.theme.AppTypography
import org.greenhouse.ui.theme.Background
import org.greenhouse.ui.theme.TextPrimary
import org.jetbrains.compose.resources.painterResource

@Composable
fun LoadingScreen() {
    val transition = rememberInfiniteTransition(label = "loading-flower")
    val scale by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1100), repeatMode = RepeatMode.Reverse),
        label = "flower-pulse",
    )

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Background)
                .padding(WindowInsets.safeDrawing.asPaddingValues()),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Image(
                painter = painterResource(Res.drawable.loading_flower),
                contentDescription = null,
                modifier =
                    Modifier
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }.size(width = 72.dp, height = 88.dp),
            )
            Text(
                text = "Florescendo...",
                color = TextPrimary,
                style = AppTypography().bodyLarge.copy(fontSize = 20.sp),
                textAlign = TextAlign.Center,
            )
        }
    }
}
