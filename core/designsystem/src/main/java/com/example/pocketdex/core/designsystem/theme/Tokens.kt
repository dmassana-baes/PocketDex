package com.example.pocketdex.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object PocketDexColors {
    val White = Color(0xFFFFFFFF)
    val Gray100 = Color(0xFFF2F4F7)
    val Gray200 = Color(0xFFDFE4EC)
    val Gray300 = Color(0xFF8D9DB9)
    val Gray400 = Color(0xFF4E5F7E)
    val Gray500 = Color(0xFF283141)
    val Gray600 = Color(0xFF171C26)
    val Black = Color(0xFF000000)
    val Yellow = Color(0xFFFFCC00)
}

object PocketDexSpacing {
    val ExtraSmall = 4.dp
    val Small = 8.dp
    val Medium = 12.dp
    val Large = 16.dp
    val Screen = 24.dp
    val TouchTarget = 48.dp
    val Icon = 24.dp
    val TypeIcon = 16.dp
    val CardImage = 56.dp
    val DetailImage = 260.dp
}

object PocketDexShapes {
    val Container = RoundedCornerShape(16.dp)
    val Chip = RoundedCornerShape(8.dp)
    val Pill = RoundedCornerShape(percent = 50)
    val Sheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
}
