@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import greenhouse.shared.generated.resources.Res
import greenhouse.shared.generated.resources.icon_ajuda
import greenhouse.shared.generated.resources.icon_avatar
import greenhouse.shared.generated.resources.icon_logout
import org.greenhouse.ui.theme.AppTypography
import org.greenhouse.ui.theme.GreenPrimary
import org.greenhouse.ui.theme.RedPrimary
import org.greenhouse.ui.theme.Surface
import org.greenhouse.ui.theme.TextPrimary
import org.jetbrains.compose.resources.painterResource

@Composable
fun HomeHeader(
    login: String,
    profile: String,
    onHelp: () -> Unit,
    onLogout: () -> Unit,
) {
    val typography = AppTypography()

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        if (maxWidth < 500.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ProfileSummary(login, profile, Modifier.fillMaxWidth())
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    HelpButton(onHelp)
                    Spacer(Modifier.width(12.dp))
                    LogoutButton(typography, onLogout)
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ProfileSummary(login, profile, Modifier.weight(1f))
                HelpButton(onHelp)
                LogoutButton(typography, onLogout)
            }
        }
    }
}

@Composable
private fun ProfileSummary(
    login: String,
    profile: String,
    modifier: Modifier = Modifier,
) {
    val typography = AppTypography()

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(GreenPrimary),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.icon_avatar),
                contentDescription = null,
                modifier = Modifier.size(30.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = login.ifBlank { "Usuário" },
                color = TextPrimary,
                style = typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = labelPerfilUsuario(profile),
                color = TextPrimary,
                style = typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun HelpButton(onHelp: () -> Unit) {
    Button(
        onClick = onHelp,
        modifier = Modifier.size(48.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, GreenPrimary),
        colors = ButtonDefaults.buttonColors(containerColor = Surface, contentColor = GreenPrimary),
        contentPadding = PaddingValues(10.dp),
    ) {
        Image(
            painter = painterResource(Res.drawable.icon_ajuda),
            contentDescription = "Ajuda",
            modifier = Modifier.size(26.dp),
        )
    }
}

@Composable
private fun LogoutButton(
    typography: androidx.compose.material3.Typography,
    onLogout: () -> Unit,
) {
    Button(
        onClick = onLogout,
        modifier = Modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, RedPrimary.copy(alpha = 0.55f)),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFE5E5),
                contentColor = RedPrimary,
            ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(Res.drawable.icon_logout),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Text("Sair", style = typography.titleMedium)
        }
    }
}

internal fun labelPerfilUsuario(profile: String): String =
    when (profile.uppercase()) {
        "FUNCIONARIO" -> "Funcionário"
        "ADMIN" -> "Administrador"
        else -> profile
    }
