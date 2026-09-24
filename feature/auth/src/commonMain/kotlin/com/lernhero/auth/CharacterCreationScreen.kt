package com.lernhero.auth

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.auth.component.AuthBackdrop
import com.lernhero.auth.component.ButtonTone
import com.lernhero.auth.component.GameButton
import com.lernhero.auth.component.GamePanel
import com.lernhero.auth.component.GameText
import com.lernhero.auth.component.GameTextField
import com.lernhero.domain.combat.StatType
import com.lernhero.domain.player.HeroAppearance
import com.lernhero.domain.player.HeroClasses
import com.lernhero.domain.player.HeroPalette
import com.lernhero.domain.player.HeroPresets
import com.lernhero.domain.player.StarterGift
import com.lernhero.domain.player.StarterGifts
import com.lernhero.game.character.HeroLook
import com.lernhero.game.character.KnightEquipment
import com.lernhero.game.character.ModularKnight
import com.lernhero.shared.i18n.strings
import com.lernhero.shared.theme.AppPalette
import com.lernhero.shared.theme.FontFirst
import org.koin.compose.viewmodel.koinViewModel

/**
 * Tworzenie postaci: JEDEN duzy bohater na scenie i jedna karta z wyborami pod nim. Zadnych
 * zakladek ani suwakow - kazde ustawienie widac od razu na tej samej figurze, a imie wisi nad
 * glowa bohatera, zanim jeszcze gracz skonczy je pisac.
 *
 * Ten sam [ModularKnight], ktorym rysuje sie bohatera w walce i w ekwipunku - to, co gracz tu
 * sklada, jest doslownie postacia, ktora zaraz zobaczy w grze.
 *
 * [devPreview] to wejscie z przycisku deweloperskiego na ekranie logowania: nikt nie jest
 * zalogowany, wiec ekran NICZEGO nie zapisuje - przycisk startu tylko wraca.
 */
@Composable
fun CharacterCreationScreen(
    navigateToHome: () -> Unit,
    devPreview: Boolean = false,
    onDevPreviewExit: () -> Unit = {}
) {
    val viewModel = koinViewModel<AuthViewModel>()
    val t = strings

    var name by remember { mutableStateOf("") }
    // Jedna wersja postaci - wybor plci wroci, gdy kobieta dostanie wlasne sprite'y. Kolor wlosow
    // tez na razie schowany (barwienie wygladalo zle) - zostaje domyslny odcien z presetu.
    val appearanceBase = remember { HeroPresets.appearanceOf(HeroPresets.Default) }
    var appearance by remember { mutableStateOf(appearanceBase) }
    var gift by remember { mutableStateOf(StarterGifts.Default) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun start() {
        if (devPreview) {
            onDevPreviewExit()
            return
        }
        val heroName = name.trim()
        if (heroName.isEmpty()) {
            errorMessage = t.creationErrorNoName
            return
        }
        isSaving = true
        errorMessage = null
        viewModel.createPlayer(
            user = viewModel.currentFirebaseUser(),
            // Klasa bohatera nie jest juz wyborem gracza - postac rozwija sie przez talenty
            // i ekwipunek, a nie przez zadeklarowanie klasy na starcie.
            character = HeroClasses.Knight,
            name = heroName,
            appearance = appearance,
            starterGift = gift,
            onSuccess = {
                isSaving = false
                navigateToHome()
            },
            onFailure = { error ->
                isSaving = false
                errorMessage = error
            }
        )
    }

    AuthBackdrop {
        // Ekran NIE przewija sie - scena bierze [Modifier.weight], wiec calosc zawsze miesci sie
        // w jednym widoku, a bohater skaluje sie z wolna wysokoscia.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GameText(text = t.creationTitle, size = 30.sp, color = Color(0xFFFFD34D), strokeWidth = 8f)

            if (devPreview) {
                Text("DEV PREVIEW - nothing is saved", color = AppPalette.Warning, fontFamily = FontFirst(), fontSize = 12.sp)
            }

            HeroStage(
                appearance = appearance,
                name = name.trim().ifEmpty { t.creationNamePlaceholder },
                named = name.isNotBlank(),
                hint = t.creationTapHero,
                // Dar startowy jest na postaci od pierwszej sekundy gry, wiec zakladamy go juz tutaj.
                equipment = gift.previewEquipment(),
                modifier = Modifier.weight(1f)
            )

            GamePanel(modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GameTextField(
                        value = name,
                        onValueChange = { newValue ->
                            if (newValue.length <= 16) name = newValue
                            errorMessage = null
                        },
                        placeholder = t.creationNameHint,
                        icon = "✏️",
                        imeAction = ImeAction.Done,
                        modifier = Modifier.weight(1f)
                    )
                    GameButton(
                        text = "🎲",
                        onClick = {
                            name = HeroNames.random(exclude = name)
                            errorMessage = null
                        },
                        tone = ButtonTone.Gold,
                        modifier = Modifier.size(54.dp)
                    )
                }

                SkinRow(
                    label = t.creationSkin,
                    colors = HeroPalette.SkinTones,
                    selected = appearance.skinTone,
                    onSelect = { appearance = appearance.copy(skinTone = it) }
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GameText(text = t.creationGiftTitle, size = 16.sp, color = Color(0xFFFFD34D), strokeWidth = 5f)
                        Text(t.creationGiftHint, color = Color(0xFFBFA878), fontFamily = FontFirst(), fontSize = 11.sp)
                    }
                    // IntrinsicSize.Min - karty maja rozne dlugosci opisow, a bez tego kazda jest
                    // innej wysokosci i najdluzsza sie przycina.
                    Row(
                        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StarterGifts.all.forEach { option ->
                            GiftCard(
                                gift = option,
                                selected = option.id == gift.id,
                                onClick = { gift = option },
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            )
                        }
                    }
                }

                errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = Color(0xFFFF8A73),
                        fontFamily = FontFirst(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                GameButton(
                    text = if (devPreview) t.commonBack else t.creationStart,
                    onClick = ::start,
                    tone = ButtonTone.Green,
                    loading = isSaving,
                    height = 58.dp,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = t.creationSubtitle,
                    color = Color(0xFF9D8BB8),
                    fontFamily = FontFirst(),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Scena: bohater na swietlistym podescie, nad glowa tabliczka z imieniem. Swiatlo za postacia
 * lekko oddycha - scena ma zyc, zanim gracz cokolwiek kliknie.
 */
@Composable
private fun HeroStage(
    appearance: HeroAppearance,
    name: String,
    named: Boolean,
    hint: String,
    equipment: KnightEquipment,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition()
    val breath by infinite.animateFloat(0.55f, 0.9f, infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse))

    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f)
                .graphicsLayer { alpha = breath }
                .background(Brush.radialGradient(listOf(Color(0x66FFC94D), Color(0x22B061FF), Color.Transparent)))
        )
        // Podest - plaska elipsa pod stopami, na ktorej bohater "stoi".
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 18.dp)
                .fillMaxWidth(0.55f)
                .height(22.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0xAAFFD34D), Color(0x33FFD34D), Color.Transparent)))
        )
        ModularKnight(
            equipment = equipment,
            look = HeroLook.of(appearance),
            sizeDp = 300.dp,
            // Klikniecie w postac = zamach mieczem - jedyny sposob, zeby zobaczyc bohatera
            // w ruchu przed startem gry.
            interactive = true,
            modifier = Modifier.fillMaxHeight().aspectRatio(1f)
        )
        Column(
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xCC120B1E))
                    .border(1.5.dp, if (named) Color(0xFFFFD34D) else Color(0xFF5A4630), RoundedCornerShape(50))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                GameText(
                    text = name,
                    size = 18.sp,
                    color = if (named) Color.White else Color(0xFF8C7C63),
                    strokeWidth = 5f
                )
            }
        }
        Text(
            text = hint,
            color = Color(0xFFCDB9E6),
            fontFamily = FontFirst(),
            fontSize = 11.sp,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/** Rzad odcieni skory - klikniecie od razu przemalowuje bohatera na scenie. */
@Composable
private fun SkinRow(
    label: String,
    colors: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        GameText(text = label, size = 16.sp, color = Color(0xFFFFD34D), strokeWidth = 5f)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            colors.forEach { hex ->
                val isSelected = hex == selected
                val scale by animateFloatAsState(if (isSelected) 1.15f else 1f)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .graphicsLayer { scaleX = scale; scaleY = scale }
                        .clip(CircleShape)
                        .background(Color(HeroPalette.parseHex(hex)))
                        .border(if (isSelected) 3.dp else 1.5.dp, if (isSelected) Color(0xFFFFD34D) else Color(0xFF120B05), CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelect(hex) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) Text("✓", color = Color(0xFF3A2410), fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun GiftCard(
    gift: StarterGift,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val item = gift.item
    val scale by animateFloatAsState(if (selected) 1.03f else 1f)
    Column(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (selected) Brush.verticalGradient(listOf(Color(0xFF4A3212), Color(0xFF2A1A0A)))
                else Brush.verticalGradient(listOf(Color(0xCC1E1428), Color(0xCC120B1A)))
            )
            .border(if (selected) 2.5.dp else 1.5.dp, if (selected) Color(0xFFFFD34D) else Color(0xFF5A4630), RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(gift.icon(), fontSize = 26.sp)
        Text(
            text = item?.name.orEmpty(),
            color = if (selected) Color(0xFFFFD34D) else Color(0xFFEADFC8),
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Black,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
        Text(
            text = gift.statLine(),
            color = Color(0xFF8BE05A),
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
    }
}

/* --------------------------------- pomocnicze --------------------------------- */

/**
 * Podglad daru na postaci. Rysowanie bohatera zna tylko sloty ("cos w rece"), nie konkretne
 * przedmioty - patrz [KnightEquipment].
 */
private fun StarterGift.previewEquipment(): KnightEquipment = when (id) {
    StarterGifts.Sword.id -> KnightEquipment(mainHand = true)
    StarterGifts.Armor.id -> KnightEquipment(chest = true)
    StarterGifts.Boots.id -> KnightEquipment(feet = true)
    else -> KnightEquipment.Empty
}

private fun StarterGift.icon(): String = when (id) {
    StarterGifts.Sword.id -> "⚔️"
    StarterGifts.Armor.id -> "🦺"
    StarterGifts.Boots.id -> "🥾"
    else -> "🎁"
}

/** Krotko, co dar daje - "Atk +5", "HP +12 Def +1" - zeby wybor nie byl w ciemno. */
private fun StarterGift.statLine(): String {
    val stats = item?.stats ?: return ""
    return listOf(
        StatType.Attack to "Atk",
        StatType.MaxHp to "HP",
        StatType.Defense to "Def",
        StatType.Speed to "Spd"
    ).mapNotNull { (stat, label) ->
        stats[stat].takeIf { it != 0f }?.let { "$label +${it.toInt()}" }
    }.joinToString(" ")
}

private object HeroNames {
    private val all = listOf(
        "Aldric", "Brynn", "Cedric", "Dorian", "Elowen", "Faelan", "Garrick", "Isolde",
        "Kael", "Lyra", "Merrin", "Nyx", "Orin", "Rowan", "Seren", "Thorne", "Valen", "Wren"
    )

    fun random(exclude: String): String = all.filter { it != exclude }.random()
}
