@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import greenhouse.shared.generated.resources.Res
import greenhouse.shared.generated.resources.icon_confirmar_light
import greenhouse.shared.generated.resources.icon_erro
import greenhouse.shared.generated.resources.icon_excluir_light
import kotlinx.coroutines.delay
import org.greenhouse.ui.theme.AppTypography
import org.greenhouse.ui.theme.GreenPrimary
import org.greenhouse.ui.theme.RedPrimary
import org.greenhouse.ui.theme.White
import org.jetbrains.compose.resources.painterResource
import org.greenhouse.ui.theme.Surface as ToastSurfaceColor

enum class ToastNotificationType {
    SUCCESS,
    DESTRUCTIVE,
    ERROR,
}

data class ToastNotificationData(
    val message: String,
    val type: ToastNotificationType,
)

@Composable
fun ToastNotification(
    notification: ToastNotificationData?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var lastNotification by remember { mutableStateOf<ToastNotificationData?>(null) }
    val currentOnDismiss by rememberUpdatedState(onDismiss)

    LaunchedEffect(notification) {
        if (notification != null) {
            lastNotification = notification
            delay(3_000)
            currentOnDismiss()
        }
    }

    AnimatedVisibility(
        visible = notification != null,
        modifier = modifier,
        enter =
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(250),
            ) + fadeIn(animationSpec = tween(250)),
        exit =
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(200),
            ) + fadeOut(animationSpec = tween(200)),
    ) {
        (notification ?: lastNotification)?.let { toast ->
            val (icon, backgroundColor, textColor) =
                when (toast.type) {
                    ToastNotificationType.SUCCESS -> Triple(Res.drawable.icon_confirmar_light, GreenPrimary, White)
                    ToastNotificationType.DESTRUCTIVE -> Triple(Res.drawable.icon_excluir_light, RedPrimary, White)
                    ToastNotificationType.ERROR -> Triple(Res.drawable.icon_erro, ToastSurfaceColor, RedPrimary)
                }

            Surface(
                modifier = Modifier.widthIn(max = 420.dp),
                shape = RoundedCornerShape(14.dp),
                color = backgroundColor,
                shadowElevation = 8.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(icon),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        text = toast.message,
                        color = textColor,
                        style = AppTypography().bodyMedium,
                    )
                }
            }
        }
    }
}
