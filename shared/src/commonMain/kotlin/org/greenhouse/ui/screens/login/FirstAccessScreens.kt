@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import greenhouse.shared.generated.resources.Res
import greenhouse.shared.generated.resources.login_background_desktop
import greenhouse.shared.generated.resources.login_background_mobile
import org.greenhouse.ui.components.ArrowDirection
import org.greenhouse.ui.components.ArrowIcon
import org.greenhouse.ui.theme.AppTypography
import org.greenhouse.ui.theme.GreenPrimary
import org.greenhouse.ui.theme.Surface
import org.greenhouse.ui.theme.TextPrimary
import org.jetbrains.compose.resources.painterResource

@Composable
fun FirstAccessLoginScreen(
    initialLogin: String,
    onBack: () -> Unit,
    onSubmit: (String) -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
) {
    var login by remember { mutableStateOf(initialLogin) }
    var validationMessage by remember { mutableStateOf<String?>(null) }
    var dismissBackendError by remember { mutableStateOf(false) }

    FirstAccessLayout(
        title = "Primeiro acesso",
        subtitle = "Informe o login cadastrado pelo administrador.",
        actionText = "Continuar",
        loadingText = "Verificando...",
        isLoading = isLoading,
        errorMessage = validationMessage ?: errorMessage.takeUnless { dismissBackendError },
        onBack = onBack,
        onAction = {
            if (login.isBlank()) {
                validationMessage = "Informe seu login."
            } else {
                validationMessage = null
                dismissBackendError = false
                onSubmit(login)
            }
        },
    ) {
        FirstAccessField(
            label = "Login (parte antes do @)",
            value = login,
            onValueChange = {
                login = it
                validationMessage = null
                dismissBackendError = true
            },
            placeholder = "Ex.: ana.lima",
            keyboardType = KeyboardType.Text,
            isLoading = isLoading,
        )
    }
}

@Composable
fun FirstAccessPasswordScreen(
    login: String,
    onBack: () -> Unit,
    onSubmit: (String, String, String) -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
) {
    val typography = AppTypography()
    var cpf by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirmation by remember { mutableStateOf("") }
    var validationMessage by remember { mutableStateOf<String?>(null) }
    var dismissBackendError by remember { mutableStateOf(false) }

    FirstAccessLayout(
        title = "Defina sua senha",
        subtitle = "Confirme seus dados cadastrados para proteger sua conta.",
        actionText = "Salvar senha",
        loadingText = "Salvando...",
        isLoading = isLoading,
        errorMessage = validationMessage ?: errorMessage.takeUnless { dismissBackendError },
        onBack = onBack,
        onAction = {
            validationMessage =
                when {
                    cpf.length != 11 -> "Informe os 11 dígitos do CPF cadastrado."
                    email.isBlank() -> "Informe o e-mail cadastrado."
                    password.length < 6 -> "A senha deve ter no mínimo 6 caracteres."
                    passwordConfirmation != password -> "As senhas não conferem."
                    else -> null
                }
            if (validationMessage == null) {
                dismissBackendError = false
                onSubmit(cpf, email, password)
            }
        },
    ) {
        Text(
            text = "Login: $login",
            color = GreenPrimary,
            style = typography.bodyMedium,
        )
        Spacer(Modifier.height(12.dp))
        FirstAccessField(
            label = "CPF cadastrado",
            value = cpf,
            onValueChange = {
                cpf = it.filter(Char::isDigit).take(11)
                validationMessage = null
                dismissBackendError = true
            },
            placeholder = "Informe os 11 dígitos do CPF",
            keyboardType = KeyboardType.Number,
            isLoading = isLoading,
        )
        Spacer(Modifier.height(12.dp))
        FirstAccessField(
            label = "E-mail cadastrado",
            value = email,
            onValueChange = {
                email = it
                validationMessage = null
                dismissBackendError = true
            },
            placeholder = "email@exemplo.com",
            keyboardType = KeyboardType.Email,
            isLoading = isLoading,
        )
        Spacer(Modifier.height(12.dp))
        FirstAccessField(
            label = "Nova senha",
            value = password,
            onValueChange = {
                password = it
                validationMessage = null
                dismissBackendError = true
            },
            placeholder = "Mínimo de 6 caracteres",
            keyboardType = KeyboardType.Password,
            isPassword = true,
            isLoading = isLoading,
        )
        Spacer(Modifier.height(12.dp))
        FirstAccessField(
            label = "Confirme a nova senha",
            value = passwordConfirmation,
            onValueChange = {
                passwordConfirmation = it
                validationMessage = null
                dismissBackendError = true
            },
            placeholder = "Digite a senha novamente",
            keyboardType = KeyboardType.Password,
            isPassword = true,
            isLoading = isLoading,
        )
    }
}

@Composable
private fun FirstAccessLayout(
    title: String,
    subtitle: String,
    actionText: String,
    loadingText: String,
    isLoading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onAction: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val typography = AppTypography()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val desktop = maxWidth >= 700.dp
        val compact = maxHeight < 640.dp
        val cardWidth =
            if (desktop) {
                (maxWidth * 0.64f).coerceAtMost(1160.dp)
            } else {
                (maxWidth - 32.dp).coerceAtMost(440.dp)
            }
        val cardHeight =
            if (desktop) {
                (maxHeight * if (compact) 0.9f else 0.82f).coerceAtMost(720.dp)
            } else {
                (maxHeight - 32.dp).coerceAtMost(560.dp)
            }
        val horizontalPadding = if (desktop) 64.dp else 24.dp
        val verticalPadding = if (desktop) 28.dp else 20.dp
        val actionHeight = if (desktop) 54.dp else 48.dp

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
            Column(
                modifier =
                    Modifier
                        .width(cardWidth)
                        .height(cardHeight)
                        .clip(RoundedCornerShape(if (desktop) 44.dp else 38.dp))
                        .background(Color.White)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = horizontalPadding, vertical = verticalPadding),
            ) {
                Text(
                    text = title,
                    color = GreenPrimary,
                    style = typography.headlineMedium.copy(fontSize = if (desktop) 28.sp else 22.sp),
                )
                Text(
                    text = subtitle,
                    color = TextPrimary,
                    style = typography.bodyLarge.copy(fontSize = if (desktop) 16.sp else 14.sp),
                )
                Spacer(Modifier.height(if (desktop) 20.dp else 16.dp))
                content()
                if (errorMessage != null) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = errorMessage,
                        color = Color(0xFFB3261E),
                        style = typography.bodySmall.copy(fontSize = if (desktop) 14.sp else 12.sp),
                    )
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onAction,
                    enabled = !isLoading,
                    modifier =
                        Modifier
                            .fillMaxWidth(if (desktop) 0.62f else 1f)
                            .height(actionHeight)
                            .align(Alignment.CenterHorizontally),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary, contentColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 16.dp),
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
                            Text(loadingText, style = typography.titleMedium.copy(fontSize = if (desktop) 17.sp else 14.sp))
                        } else {
                            Text(actionText, style = typography.titleMedium.copy(fontSize = if (desktop) 17.sp else 14.sp))
                            ArrowIcon(
                                direction = ArrowDirection.Right,
                                color = Color.White,
                                modifier = Modifier.width(20.dp).height(16.dp),
                            )
                        }
                    }
                }
            }

            androidx.compose.material3.IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.TopStart).padding(6.dp).size(40.dp),
                enabled = !isLoading,
            ) {
                ArrowIcon(
                    direction = ArrowDirection.Left,
                    color = GreenPrimary,
                    modifier = Modifier.size(24.dp),
                    contentDescription = "Voltar",
                )
            }
        }
    }
}

@Composable
private fun FirstAccessField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    isLoading: Boolean,
    isPassword: Boolean = false,
) {
    val typography = AppTypography()

    Column {
        Text(label, color = TextPrimary, style = typography.bodyMedium)
        Spacer(Modifier.height(4.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled = !isLoading,
            placeholder = { Text(placeholder, color = TextPrimary.copy(alpha = 0.48f)) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors =
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
                ),
        )
    }
}
