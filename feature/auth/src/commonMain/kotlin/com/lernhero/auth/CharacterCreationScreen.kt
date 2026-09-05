package com.lernhero.auth

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
import com.lernhero.shared.theme.FontPixel
import com.lernhero.shared.theme.FontSize
import org.koin.compose.viewmodel.koinViewModel

/**
 * Tworzenie postaci: JEDEN duzy bohater i kilka rzedow kafelkow pod nim. Zadnych zakladek ani
 * suwakow - kazde ustawienie widac od razu na tej samej figurze, wiec gracz nie musi niczego
 * sobie wyobrazac.
 *
 * Ten sam [ModularKnight], ktorym rysuje sie bohatera w walce i w ekwipunku - to, co gracz tu
 * sklada, jest doslownie postacia, ktora zaraz zobaczy w grze.
 *
 * [devPreview] to wejscie z przycisku deweloperskiego na ekranie logowania: nikt nie jest
 * zalogowany, wiec ekran NICZEGO nie zapisuje - przycisk startu tylko wraca. Bez tego "Ruszaj na
 * przygode" konczyloby sie bledem "User is not available", ktory nie mowi nic o samym ekranie.
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

    // Ekran NIE przewija sie - HeroStage bierze [Modifier.weight], wiec cala kolumna zawsze
    // miesci sie w jednym widoku niezaleznie od wysokosci telefonu.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppPalette.ScreenGradient)
            .safeDrawingPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = t.creationTitle,
            color = AppPalette.Gold,
            fontFamily = FontPixel(),
            fontSize = FontSize.LARGE
        )

        if (devPreview) {
            Text(
                text = "DEV PREVIEW - nic sie nie zapisuje",
                color = AppPalette.Warning,
                fontFamily = FontFirst(),
                fontSize = FontSize.SMALL
            )
        }

        HeroStage(
            appearance = appearance,
            // Dar startowy jest na postaci od pierwszej sekundy gry, wiec zakladamy go juz tutaj.
            // Helm nie ma jeszcze wlasnego sprite'a (patrz Resources.PlateArmor), wiec jego
            // wybor po prostu nic nie zmienia w podgladzie - dolozenie grafiki naprawi to samo.
            equipment = gift.previewEquipment(),
            modifier = Modifier.weight(1f)
        )

        SwatchRow(
            label = t.creationSkin,
            colors = HeroPalette.SkinTones,
            selected = appearance.skinTone,
            onSelect = { appearance = appearance.copy(skinTone = it) }
        )

        OutlinedTextField(
            value = name,
            onValueChange = { newValue -> if (newValue.length <= 16) name = newValue },
            singleLine = true,
            label = { Text(text = t.creationNameLabel, fontFamily = FontFirst()) },
            placeholder = { Text(text = t.creationNameHint, fontFamily = FontFirst()) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = AppPalette.TextBody,
                unfocusedTextColor = AppPalette.TextBody,
                focusedBorderColor = AppPalette.Gold,
                unfocusedBorderColor = AppPalette.GoldDim,
                focusedLabelColor = AppPalette.Gold,
                unfocusedLabelColor = AppPalette.TextMuted,
                cursorColor = AppPalette.Gold
            ),
            modifier = Modifier.fillMaxWidth()
        )

        SectionLabel(text = t.creationGiftTitle, hint = t.creationGiftHint)
        // IntrinsicSize.Min - trzy karty maja rozne dlugosci opisow, a bez tego kazda jest innej
        // wysokosci i najdluzsza sie przycina.
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
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

        Text(
            text = t.creationSubtitle,
            color = AppPalette.TextFaint,
            fontFamily = FontFirst(),
            fontSize = FontSize.SMALL,
            textAlign = TextAlign.Center
        )

        errorMessage?.let { message ->
            Text(
                text = message,
                color = AppPalette.Danger,
                fontFamily = FontFirst(),
                fontSize = FontSize.SMALL
            )
        }

        Button(
            onClick = {
                if (devPreview) {
                    onDevPreviewExit()
                    return@Button
                }
                val heroName = name.trim()
                if (heroName.isEmpty()) {
                    errorMessage = t.creationErrorNoName
                    return@Button
                }
                isSaving = true
                errorMessage = null
                viewModel.createPlayer(
                    user = viewModel.currentFirebaseUser(),
                    // Klasa bohatera nie jest juz wyborem gracza - postac rozwija sie przez
                    // talenty i ekwipunek, a nie przez zadeklarowanie klasy na starcie.
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
            },
            enabled = !isSaving,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppPalette.Action,
                contentColor = AppPalette.GoldBright,
                disabledContainerColor = AppPalette.Panel,
                disabledContentColor = AppPalette.TextFaint
            ),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = AppPalette.GoldBright,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = if (devPreview) t.commonBack else t.creationStart,
                    fontFamily = FontPixel(),
                    fontSize = FontSize.EXTRA_REGULAR,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/** Scena z bohaterem - jedyny duzy element ekranu, bierze cala wolna wysokosc ([Modifier.weight]). */
@Composable
private fun HeroStage(
    appearance: HeroAppearance,
    equipment: KnightEquipment,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(AppPalette.CardGradient)
            .border(2.dp, AppPalette.GoldFrame, RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        ModularKnight(
            equipment = equipment,
            look = HeroLook.of(appearance),
            sizeDp = 300.dp,
            // Klikniecie w postac = zamach mieczem. Jedyna "zabawka" na tym ekranie i jedyny
            // sposob, zeby zobaczyc bohatera w ruchu przed startem gry.
            interactive = true,
            // Kwadrat wpisany w scene - scena bierze zmienna wysokosc, wiec postac skaluje sie
            // z nia, zamiast wystawac przy nizszym telefonie (dawne requiredSize(300)).
            modifier = Modifier.fillMaxHeight().aspectRatio(1f)
        )
    }
}

/** Rzad kolorow do wyboru - klikniecie od razu przemalowuje bohatera nad spodem ekranu. */
@Composable
private fun SwatchRow(
    label: String,
    colors: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            color = AppPalette.TextMuted,
            fontFamily = FontFirst(),
            fontSize = FontSize.SMALL
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            colors.forEach { hex ->
                val isSelected = hex == selected
                val borderColor by animateColorAsState(
                    targetValue = if (isSelected) AppPalette.Gold else AppPalette.GoldDim
                )
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 34.dp else 30.dp)
                        .clip(CircleShape)
                        .background(Color(HeroPalette.parseHex(hex)))
                        .border(if (isSelected) 3.dp else 1.dp, borderColor, CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelect(hex) }
                        )
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String, hint: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = text,
            color = AppPalette.Gold,
            fontFamily = FontPixel(),
            fontSize = FontSize.EXTRA_REGULAR
        )
        Text(
            text = hint,
            color = AppPalette.TextFaint,
            fontFamily = FontFirst(),
            fontSize = FontSize.EXTRA_SMALL
        )
    }
}

@Composable
private fun GiftCard(
    gift: StarterGift,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) AppPalette.Gold else AppPalette.GoldDim
    )
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) AppPalette.PanelRaised else AppPalette.Panel)
            .border(if (selected) 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = gift.item?.name.orEmpty(),
            color = if (selected) AppPalette.Gold else AppPalette.TextBody,
            fontFamily = FontPixel(),
            fontSize = FontSize.SMALL,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = gift.item?.description.orEmpty(),
            color = AppPalette.TextFaint,
            fontFamily = FontFirst(),
            fontSize = FontSize.EXTRA_SMALL
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
    StarterGifts.Helm.id -> KnightEquipment(head = true)
    StarterGifts.Boots.id -> KnightEquipment(feet = true)
    else -> KnightEquipment.Empty
}
