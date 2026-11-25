@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import greenhouse.shared.generated.resources.Res
import greenhouse.shared.generated.resources.icon_coffee
import org.greenhouse.ui.theme.AppTypography
import org.greenhouse.ui.theme.TextPrimary
import org.jetbrains.compose.resources.painterResource

@Composable
fun Footer(modifier: Modifier = Modifier) {
    val typography = AppTypography()
    val footerInlineContent =
        mapOf(
            "coffee" to
                InlineTextContent(
                    Placeholder(
                        width = 20.sp,
                        height = 20.sp,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
                    ),
                ) {
                    Image(
                        painter = painterResource(Res.drawable.icon_coffee),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                    )
                },
        )

    Text(
        text =
            buildAnnotatedString {
                withStyle(SpanStyle(fontSize = 14.sp, color = TextPrimary)) {
                    append("\u00A9")
                }
                append(" Copyright Greenhouse 2026. All Rights Reserved. Make with ")
                appendInlineContent("coffee", "[coffee]")
                append(" by Lovelace.")
            },
        modifier = modifier.fillMaxWidth(),
        color = TextPrimary,
        style = typography.bodySmall,
        textAlign = TextAlign.Center,
        inlineContent = footerInlineContent,
    )
}
