package org.greenhouse.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import greenhouse.shared.generated.resources.Res
import greenhouse.shared.generated.resources.poppins_bold
import greenhouse.shared.generated.resources.poppins_light
import greenhouse.shared.generated.resources.poppins_regular
import greenhouse.shared.generated.resources.poppins_semibold
import greenhouse.shared.generated.resources.protest_riot_regular
import org.jetbrains.compose.resources.Font

@Composable
fun AppTypography(): Typography {
    val protestRiot = FontFamily(
        Font(
            resource = Res.font.protest_riot_regular,
            weight = FontWeight.Normal
        )
    )

    val poppins = FontFamily(
        Font(Res.font.poppins_light, FontWeight.Light),
        Font(Res.font.poppins_regular, FontWeight.Normal),
        Font(Res.font.poppins_semibold, FontWeight.SemiBold),
        Font(Res.font.poppins_bold, FontWeight.Bold)
    )

    return Typography(
        displayLarge = TextStyle(
            fontFamily = protestRiot,
            fontSize = 40.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = protestRiot,
            fontSize = 32.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = protestRiot,
            fontSize = 26.sp
        ),
        titleLarge = TextStyle(
            fontFamily = poppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp
        ),
        titleMedium = TextStyle(
            fontFamily = poppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = poppins,
            fontSize = 16.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = poppins,
            fontSize = 14.sp
        ),
        bodySmall = TextStyle(
            fontFamily = poppins,
            fontWeight = FontWeight.Light,
            fontSize = 12.sp
        ),
        labelLarge = TextStyle(
            fontFamily = poppins,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    )
}