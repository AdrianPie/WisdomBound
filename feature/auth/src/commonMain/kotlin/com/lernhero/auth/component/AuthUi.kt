package com.lernhero.auth.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.shared.resources.Resources
import com.lernhero.shared.theme.AppPalette
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.StrokedText
import org.jetbrains.compose.resources.painterResource

/*
 * Wspolny wyglad ekranow wejscia (logowanie, tworzenie postaci) - ten sam jezyk co reszta gry:
 * niebo o zmierzchu w tle, grube napisy z obrysem, przyciski z gradientem i ciemna oprawa,
 * zlote ramki. Wczesniej te ekrany byly plaska ciemna kartka z polami Material, ktora wygladala
 * jak inna aplikacja niz to, co gracz widzial zaraz po zalogowaniu.
 */

val PanelFill = Color(0xE6180F22)
val PanelEdge = Color(0xFFC8A45A)
private val Outline = Color(0xFF120B05)

/** Tlo gry (to samo niebo co w Swiatyni) z przyciemnieniem u dolu pod formularz. */
@Composable
fun AuthBackdrop(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier = modifier.fillMaxSize().background(Color(0xFF1B1030))) {
        Image(
            painter = painterResource(Resources.Image.appBackground),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0x00000000), Color(0x55000000), Color(0xCC0B0612))))
        )
        content()
    }
}

/** Gruby napis z obrysem - naglowki i napisy na przyciskach. */
@Composable
fun GameText(
    text: String,
    size: TextUnit,
    color: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 6f
) {
    StrokedText(text, size, color, Outline, modifier, strokeWidth)
}

/** Polprzezroczysta karta ze zlota oprawa, na ktorej leza formularze. */
@Composable
fun GamePanel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(PanelFill)
            .border(2.dp, Brush.verticalGradient(listOf(Color(0xFFFFE28A), PanelEdge, Color(0xFF7A5A2A))), RoundedCornerShape(22.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

enum class ButtonTone(val top: Color, val bottom: Color) {
    Gold(Color(0xFFFFD34D), Color(0xFFE08A1E)),
    Green(Color(0xFF8BE05A), Color(0xFF3F9E2B)),
    Plain(Color(0xFFF4EEE4), Color(0xFFD8CEBF))
}

/** Przycisk w stylu gry: gradient, ciemna oprawa, lekkie wcisniecie pod palcem. */
@Composable
fun GameButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tone: ButtonTone = ButtonTone.Gold,
    loading: Boolean = false,
    enabled: Boolean = true,
    height: Dp = 54.dp,
    leading: (@Composable () -> Unit)? = null,
    textColor: Color = Color.White
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, tween(90))
    val active = enabled && !loading
    Box(
        modifier = modifier
            .height(height)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (active) Brush.verticalGradient(listOf(tone.top, tone.bottom))
                else Brush.verticalGradient(listOf(Color(0xFF6F6A64), Color(0xFF4A4642)))
            )
            .border(2.5.dp, Outline, RoundedCornerShape(14.dp))
            .clickable(interactionSource = interaction, indication = null, enabled = active, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Blik na gornej polowie - to on robi z plaskiego prostokata "guzik z gry".
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height / 2)
                .align(Alignment.TopCenter)
                .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.28f), Color.Transparent)))
        )
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.5.dp)
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                leading?.invoke()
                if (tone == ButtonTone.Plain) {
                    Text(text, color = textColor, fontFamily = FontFirst(), fontWeight = FontWeight.Black, fontSize = 16.sp)
                } else {
                    GameText(text = text, size = 17.sp, color = textColor, strokeWidth = 5f)
                }
            }
        }
    }
}

/** Przycisk Google w tym samym ksztalcie co reszta - biala tabliczka z logo, jak wymaga Google. */
@Composable
fun GoogleSignInButton(text: String, loading: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    GameButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        tone = ButtonTone.Plain,
        loading = false,
        enabled = !loading,
        textColor = Color(0xFF2A2A2A),
        leading = {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFF4285F4), strokeWidth = 2.dp)
            } else {
                Icon(
                    painter = painterResource(Resources.Image.GoogleLogo),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    )
}

/**
 * Pole formularza: ciemne wglebienie z ikona z lewej. Zlota ramka zapala sie, gdy pole ma fokus -
 * gracz od razu widzi, gdzie pisze.
 */
@Composable
fun GameTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: String,
    modifier: Modifier = Modifier,
    password: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
    trailing: (@Composable () -> Unit)? = null
) {
    var focused by remember { mutableStateOf(false) }
    var reveal by remember { mutableStateOf(false) }
    val edge = if (focused) Color(0xFFFFD34D) else Color(0xFF5A4630)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xCC0D0812))
            .border(if (focused) 2.dp else 1.5.dp, edge, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(icon, fontSize = 18.sp)
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (value.isEmpty()) {
                Text(placeholder, color = Color(0xFF8C7C63), fontFamily = FontFirst(), fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(color = AppPalette.TextBody, fontFamily = FontFirst(), fontWeight = FontWeight.Bold, fontSize = 16.sp),
                cursorBrush = SolidColor(Color(0xFFFFD34D)),
                visualTransformation = if (password && !reveal) PasswordVisualTransformation() else VisualTransformation.None,
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (password) KeyboardType.Password else keyboardType,
                    imeAction = imeAction
                ),
                keyboardActions = KeyboardActions(onAny = { onImeAction() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focused = it.isFocused }
            )
        }
        if (password) {
            Text(
                text = if (reveal) "🙈" else "👁",
                fontSize = 18.sp,
                modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { reveal = !reveal }
            )
        }
        trailing?.invoke()
    }
}

/** Godlo z miekka, pulsujaca poswiata i lekkim unoszeniem - jedyny "zywy" element ekranu logowania. */
@Composable
fun FloatingEmblem(size: Dp, modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition()
    val glow by infinite.animateFloat(0.35f, 0.7f, infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse))
    val float by infinite.animateFloat(-4f, 4f, infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse))
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(size)
                .graphicsLayer { alpha = glow }
                .background(Brush.radialGradient(listOf(Color(0x99FFC94D), Color(0x33FF9A2E), Color.Transparent)))
        )
        Image(
            painter = painterResource(Resources.Image.emblem),
            contentDescription = "WisdomBound",
            modifier = Modifier
                .size(size * 0.72f)
                .graphicsLayer { translationY = float * density }
        )
    }
}

/** Kreska z napisem na srodku ("albo"). */
@Composable
fun LabeledDivider(label: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(Modifier.weight(1f).height(1.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, PanelEdge))))
        Text(label, color = Color(0xFFBFA878), fontFamily = FontFirst(), fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Box(Modifier.weight(1f).height(1.dp).background(Brush.horizontalGradient(listOf(PanelEdge, Color.Transparent))))
    }
}
