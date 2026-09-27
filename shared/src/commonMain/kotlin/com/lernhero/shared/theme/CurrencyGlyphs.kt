package com.lernhero.shared.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import lernhero.shared.generated.resources.Res
import lernhero.shared.generated.resources.currency_coin
import lernhero.shared.generated.resources.currency_diamon
import lernhero.shared.generated.resources.currency_sackofgold
import lernhero.shared.generated.resources.currency_thunder
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * Rysowane ikony walut w miejsce emoji.
 *
 * Kod gry od zawsze opisuje waluty emoji ("💎 50", ikona nagrody "💰") - w domenie, w nagrodach,
 * w napisach na przyciskach. Zamiast przepisywac kazde z tych miejsc, emoji zostaje umowna nazwa
 * waluty, a zamiana na obrazek dzieje sie dopiero przy rysowaniu: [GlyphIcon] dla samej ikony,
 * [GlyphText] i [StrokedText] dla napisow z ikona w srodku.
 *
 * Emoji wygladaja inaczej na kazdym telefonie (a na starszych Androidach "🪙" to pusty
 * prostokat) - obrazek jest wszedzie ten sam.
 */
enum class CurrencyGlyph(val glyph: String, val image: DrawableResource) {
    Coin("🪙", Res.drawable.currency_coin),
    GoldSack("💰", Res.drawable.currency_sackofgold),
    Gem("💎", Res.drawable.currency_diamon),
    Energy("⚡", Res.drawable.currency_thunder);

    companion object {
        /** Dopuszcza wariant z selektorem emoji (U+FE0F), ktory klawiatury dopisuja do "⚡️". */
        fun of(text: String): CurrencyGlyph? {
            val bare = text.trim().removeSuffix(VARIATION_SELECTOR)
            return entries.firstOrNull { it.glyph == bare }
        }

        fun containsAny(text: String): Boolean = entries.any { text.contains(it.glyph) }
    }
}

private const val VARIATION_SELECTOR = "️"

/**
 * Ikona podana jako emoji: waluta rysuje sie obrazkiem, kazde inne emoji (🎟, ❤️, ⏳...) zostaje
 * emoji. [size] to wielkosc obrazka; emoji dostaje odpowiadajacy mu rozmiar czcionki.
 */
@Composable
fun GlyphIcon(glyph: String, size: Dp, modifier: Modifier = Modifier) {
    val currency = CurrencyGlyph.of(glyph)
    if (currency != null) {
        Image(painter = painterResource(currency.image), contentDescription = currency.name, modifier = modifier.size(size))
    } else {
        val fontSize = with(LocalDensity.current) { (size * 0.82f).toSp() }
        Text(text = glyph, fontSize = fontSize, modifier = modifier)
    }
}

/** Obrazek w linii tekstu jest ciut wiekszy od liter - emoji tez sa wyzsze niz wersaliki. */
private val INLINE_ICON_SIZE = 1.25.em

/**
 * Tekst z emoji walut podmienionymi na obrazki w linii ("REFILL 💎 30" -> REFILL [diament] 30).
 * Null, gdy w tekscie nie ma zadnej waluty - wtedy rysujemy zwykly String bez narzutu.
 */
internal fun currencyAnnotated(text: String): Pair<AnnotatedString, Map<String, InlineTextContent>>? {
    if (!CurrencyGlyph.containsAny(text)) return null
    val used = mutableSetOf<CurrencyGlyph>()
    val annotated = buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            val match = CurrencyGlyph.entries.firstOrNull { text.startsWith(it.glyph, i) }
            if (match == null) {
                append(text[i])
                i++
                continue
            }
            used += match
            appendInlineContent(match.name, match.glyph)
            i += match.glyph.length
            if (text.startsWith(VARIATION_SELECTOR, i)) i += VARIATION_SELECTOR.length
        }
    }
    val content = used.associate { currency ->
        currency.name to InlineTextContent(
            Placeholder(INLINE_ICON_SIZE, INLINE_ICON_SIZE, PlaceholderVerticalAlign.TextCenter)
        ) {
            Image(painter = painterResource(currency.image), contentDescription = null, modifier = Modifier.fillMaxSize())
        }
    }
    return annotated to content
}

/**
 * Zamiennik [Text] dla napisow, w ktorych moze stac emoji waluty. Te same parametry co w
 * [Text], wiec przejscie to zmiana samej nazwy funkcji.
 */
@Composable
fun GlyphText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current
) {
    val rich = remember(text) { currencyAnnotated(text) }
    if (rich == null) {
        Text(
            text = text, modifier = modifier, color = color, fontSize = fontSize, fontWeight = fontWeight,
            fontFamily = fontFamily, letterSpacing = letterSpacing, textAlign = textAlign, lineHeight = lineHeight,
            overflow = overflow, maxLines = maxLines, onTextLayout = onTextLayout, style = style
        )
    } else {
        Text(
            text = rich.first, modifier = modifier, color = color, fontSize = fontSize, fontWeight = fontWeight,
            fontFamily = fontFamily, letterSpacing = letterSpacing, textAlign = textAlign, lineHeight = lineHeight,
            overflow = overflow, maxLines = maxLines, inlineContent = rich.second, onTextLayout = onTextLayout,
            style = style
        )
    }
}
