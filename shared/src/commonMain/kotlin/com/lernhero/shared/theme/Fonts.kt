package com.lernhero.shared.theme

import androidx.compose.runtime.Composable

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import lernhero.shared.generated.resources.Res
import lernhero.shared.generated.resources.pixelify_regular
import lernhero.shared.generated.resources.roboto
import lernhero.shared.generated.resources.roboto_condensed_black
import org.jetbrains.compose.resources.Font

@Composable
fun FontFirst() =
    FontFamily(
        Font(Res.font.roboto),
    )

@Composable
fun FontSecond() =
    FontFamily(
        Font(Res.font.roboto_condensed_black)
    )
@Composable
fun FontPixel() =
    FontFamily(
        Font(Res.font.pixelify_regular)
    )

object FontSize {
    val EXTRA_SMALL = 10.sp
    val SMALL = 12.sp
    val REGULAR = 14.sp
    val EXTRA_REGULAR = 16.sp
    val MEDIUM = 18.sp
    val EXTRA_MEDIUM = 20.sp
    val LARGE = 30.sp
    val EXTRA_LARGE = 40.sp
}

