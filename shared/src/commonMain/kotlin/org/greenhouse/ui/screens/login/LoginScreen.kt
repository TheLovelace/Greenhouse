@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.screens.login

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import greenhouse.shared.generated.resources.Res
import greenhouse.shared.generated.resources.login_background_desktop
import greenhouse.shared.generated.resources.login_background_mobile
import org.greenhouse.ui.components.ArrowDirection
import org.greenhouse.ui.components.ArrowIcon
import org.greenhouse.ui.theme.AppTypography
import org.greenhouse.ui.theme.GreenPrimary
import org.greenhouse.ui.theme.GreenSecondary
import org.greenhouse.ui.theme.Surface
import org.greenhouse.ui.theme.TextPrimary
import org.jetbrains.compose.resources.painterResource

@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onSubmit: (String, String, Boolean) -> Unit,
    onFirstAccess: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
) {
    val typography = AppTypography()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberDevice by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val desktop = maxWidth >= 700.dp
        val compact = maxHeight < 640.dp
        val veryCompact = maxHeight < 420.dp
        val regularDesktop = desktop && !compact
        val horizontalPadding =
            when {
                regularDesktop -> 104.dp
                desktop -> 64.dp
                else -> 20.dp
            }
        val verticalPadding =
            when {
                veryCompact -> 8.dp
                regularDesktop -> 28.dp
                desktop -> 16.dp
                compact -> 12.dp
                else -> 20.dp
            }
        val headingFontSize =
            when {
                veryCompact -> 18.sp
                regularDesktop -> 30.sp
                desktop -> 24.sp
                else -> 21.sp
            }
        val subtitleFontSize =
            when {
                veryCompact -> 12.sp
                regularDesktop -> 18.sp
                desktop -> 16.sp
                else -> 14.sp
            }
        val labelFontSize =
            when {
                regularDesktop -> 15.sp
                desktop -> 14.sp
                else -> 12.sp
            }
        val roleFontSize =
            when {
                regularDesktop -> 14.sp
                desktop -> 13.sp
                else -> 11.sp
            }
        val fieldHeight =
            when {
                veryCompact -> 44.dp
                regularDesktop -> 54.dp
                else -> 48.dp
            }
        val headerSpacing =
            when {
                veryCompact -> 6.dp
                regularDesktop -> 24.dp
                desktop -> 14.dp
                compact -> 10.dp
                else -> 18.dp
            }
        val fieldSpacing =
            when {
                veryCompact -> 6.dp
                regularDesktop -> 20.dp
                desktop -> 12.dp
                compact -> 10.dp
                else -> 14.dp
            }
        val afterFieldsSpacing =
            when {
                veryCompact -> 6.dp
                regularDesktop -> 18.dp
                desktop -> 12.dp
                compact -> 8.dp
                else -> 14.dp
            }
        val beforeButtonSpacing =
            when {
                veryCompact -> 6.dp
                regularDesktop -> 22.dp
                desktop -> 12.dp
                compact -> 8.dp
                else -> 14.dp
            }
        val buttonHeight =
            when {
                veryCompact -> 42.dp
                regularDesktop -> 54.dp
                desktop -> 48.dp
                else -> 46.dp
            }
        val cardWidth =
            if (desktop) {
                (maxWidth * 0.64f).coerceAtMost(1160.dp)
            } else {
                (maxWidth - 32.dp).coerceAtMost(440.dp)
            }
        val cardHeight =
            if (desktop) {
                (maxHeight * if (compact) 0.9f else 0.74f).coerceAtMost(760.dp)
            } else {
                (maxHeight - 32.dp).coerceAtMost(480.dp)
            }
        val cardShape = RoundedCornerShape(if (desktop) 44.dp else 38.dp)

        Image(
            painter = painterResource(if (desktop) Res.drawable.login_background_desktop else Res.drawable.login_background_mobile),
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
                        .clip(cardShape)
                        .background(Color.White),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(
                                horizontal = horizontalPadding,
                                vertical = verticalPadding,
                            ),
                ) {
                    Text(
                        text = "Bem-vindo(a) de volta",
                        color = GreenPrimary,
                        style = typography.headlineMedium.copy(fontSize = headingFontSize),
                    )
                    Text(
                        text = "Insira seus dados de login",
                        color = TextPrimary,
                        style = typography.bodyLarge.copy(fontSize = subtitleFontSize),
                    )
                    Spacer(Modifier.height(headerSpacing))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("E-mail", color = TextPrimary, style = typography.bodyMedium.copy(fontSize = labelFontSize))
                        Text(
                            "Admin/Funcionário",
                            color = TextPrimary,
                            style = typography.bodyMedium.copy(fontSize = roleFontSize),
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth().height(fieldHeight),
                        enabled = !isLoading,
                        placeholder = { Text("email@exemplo.com", color = TextPrimary.copy(alpha = 0.48f)) },
                        leadingIcon = {
                            Text("@", color = GreenPrimary, style = typography.titleMedium)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = loginFieldColors(),
                    )

                    Spacer(Modifier.height(fieldSpacing))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Senha", color = TextPrimary, style = typography.bodyMedium.copy(fontSize = labelFontSize))
                        Text(
                            "Esqueceu a senha?",
                            color = GreenSecondary,
                            style =
                                typography.bodyMedium.copy(
                                    fontSize = roleFontSize,
                                    textDecoration = TextDecoration.Underline,
                                ),
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth().height(fieldHeight),
                        enabled = !isLoading,
                        placeholder = { Text("••••••••", color = TextPrimary.copy(alpha = 0.48f)) },
                        leadingIcon = { LockGlyph() },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                EyeGlyph(visible = showPassword)
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = loginFieldColors(),
                    )

                    Spacer(Modifier.height(afterFieldsSpacing))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = rememberDevice,
                            onCheckedChange = { rememberDevice = it },
                            enabled = !isLoading,
                            modifier = Modifier.size(24.dp),
                            colors =
                                CheckboxDefaults.colors(
                                    checkedColor = GreenPrimary,
                                    uncheckedColor = Surface,
                                    checkmarkColor = Color.White,
                                ),
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Lembrar desse dispositivo?",
                            color = TextPrimary,
                            style = typography.bodyMedium.copy(fontSize = roleFontSize),
                        )
                    }
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            color = Color(0xFFB3261E),
                            style = typography.bodySmall.copy(fontSize = roleFontSize),
                        )
                    }
                    Spacer(Modifier.height(beforeButtonSpacing))
                    Button(
                        onClick = { onSubmit(email, password, rememberDevice) },
                        enabled = !isLoading,
                        modifier =
                            Modifier
                                .fillMaxWidth(if (desktop) 0.62f else 1f)
                                .height(buttonHeight)
                                .align(Alignment.CenterHorizontally),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary, contentColor = Color.White),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                )
                                Text("Entrando...", style = typography.titleMedium.copy(fontSize = if (desktop) 17.sp else 14.sp))
                            } else {
                                Text(
                                    "Entrar no sistema",
                                    style = typography.titleMedium.copy(fontSize = if (desktop) 17.sp else 14.sp),
                                    textAlign = TextAlign.Center,
                                )
                                ArrowIcon(
                                    direction = ArrowDirection.Right,
                                    color = Color.White,
                                    modifier = Modifier.width(20.dp).height(16.dp),
                                )
                            }
                        }
                    }
                    TextButton(
                        onClick = onFirstAccess,
                        enabled = !isLoading,
                        modifier =
                            Modifier
                                .fillMaxWidth(if (desktop) 0.62f else 1f)
                                .align(Alignment.CenterHorizontally),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = GreenSecondary),
                    ) {
                        Text(
                            "Primeiro acesso",
                            style =
                                typography.bodySmall.copy(
                                    fontSize = roleFontSize,
                                    textDecoration = TextDecoration.Underline,
                                ),
                        )
                    }
                }
            }

            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.TopStart).padding(6.dp).size(40.dp),
            ) {
                ArrowIcon(
                    direction = ArrowDirection.Left,
                    color = GreenPrimary,
                    modifier = Modifier.size(24.dp),
                    contentDescription = "Voltar para a tela inicial",
                )
            }
        }
    }
}

@Composable
private fun loginFieldColors() =
    TextFieldDefaults.colors(
        focusedContainerColor = Surface,
        unfocusedContainerColor = Surface,
        disabledContainerColor = Surface,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        cursorColor = GreenPrimary,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
    )

@Composable
private fun LockGlyph() {
    Canvas(modifier = Modifier.size(22.dp)) {
        val strokeWidth = 2.dp.toPx()
        drawArc(
            color = GreenPrimary,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(size.width * 0.25f, size.height * 0.04f),
            size = Size(size.width * 0.5f, size.height * 0.62f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
        drawRoundRect(
            color = GreenPrimary,
            topLeft = Offset(size.width * 0.14f, size.height * 0.42f),
            size = Size(size.width * 0.72f, size.height * 0.5f),
            cornerRadius = CornerRadius(3.dp.toPx()),
        )
    }
}

@Composable
private fun EyeGlyph(visible: Boolean) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val eyeSize = Size(size.width * 0.86f, size.height * 0.58f)
        val eyeTopLeft = Offset(size.width * 0.07f, size.height * 0.21f)
        drawOval(color = GreenPrimary, topLeft = eyeTopLeft, size = eyeSize, style = Stroke(width = 2.dp.toPx()))
        drawCircle(color = GreenPrimary, radius = size.minDimension * 0.13f)
        if (!visible) {
            drawLine(
                color = GreenPrimary,
                start = Offset(size.width * 0.12f, size.height * 0.88f),
                end = Offset(size.width * 0.88f, size.height * 0.12f),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
    }
}
