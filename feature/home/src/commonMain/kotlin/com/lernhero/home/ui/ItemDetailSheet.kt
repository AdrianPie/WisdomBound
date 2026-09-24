package com.lernhero.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.combat.StatType
import com.lernhero.domain.craft.ItemUpgrades
import com.lernhero.domain.item.GearPower
import com.lernhero.domain.item.Item
import com.lernhero.domain.jewel.Jewel
import com.lernhero.shared.theme.FontFirst

/**
 * Karta przedmiotu w stylu pergaminowym - kremowy papier w brazowej oprawie, sekcje z wlasnymi
 * belkami tytulowymi, liczby na zielono.
 *
 * Wczesniej byla to ciemna, plaska plansza: tekst gubil sie w tle, sekcje nie mialy zadnego
 * podzialu, a przyciski niosly jeszcze polskie napisy. Mobilne RPG (Shiba Inu Adventure, AFK
 * Arena) robia to inaczej i maja racje: JASNY papier, kazda informacja w swoim pudelku, jedna
 * wielka liczba mocy, po ktorej gracz porownuje przedmioty bez czytania calej tabelki.
 *
 * Uklad idzie z gory na dol dokladnie w kolejnosci decyzji gracza:
 * 1. co to jest (ikona, poziom, rzadkosc, slot),
 * 2. ile to daje (statystyki + [GearPower]),
 * 3. co to ROBI (zdolnosci - to one decyduja przy rownych liczbach),
 * 4. co z tym zrobic (zaloz/zdejmij).
 *
 * [comparedTo] to przedmiot aktualnie w tym slocie (null, gdy slot pusty) - roznice pokazuja sie
 * wtedy na zielono/czerwono przy kazdej statystyce i przy samej mocy.
 */
@Composable
fun ItemDetailSheet(
    item: Item,
    /** Poziom ulepszenia TEJ sztuki - naglowek pokazuje go jako "Lv. 3/10". */
    level: Int,
    comparedTo: Item?,
    equipped: Boolean,
    onEquip: () -> Unit,
    onUnequip: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    /** Liczba slotow na kamienie - 0 chowa cala sekcje. */
    socketCount: Int = 0,
    socketed: List<Jewel> = emptyList(),
    /** Tapniecie slotu: indeks osadzonego kamienia albo null dla pustego slotu. */
    onSocketClick: (Int?) -> Unit = {}
) {
    val rarity = RarityPalette.frame(item.rarity)
    val rows = remember(item, comparedTo) { statRows(item, comparedTo) }
    val power = remember(item) { GearPower.of(item) }
    val powerDelta = remember(item, comparedTo) { comparedTo?.let { power - GearPower.of(it) } }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xCC0B0805))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 380.dp)
                .padding(horizontal = 18.dp, vertical = 24.dp)
                .clip(CardShape)
                // Podwojna oprawa: gruby braz na zewnatrz, jasniejsza kreska w srodku - to ona
                // robi "oprawiona tablice" zamiast zwyklego prostokata.
                .background(ParchmentPalette.Frame)
                .padding(4.dp)
                .clip(InnerShape)
                .background(ParchmentPalette.Paper)
                .border(1.5.dp, ParchmentPalette.FrameLight.copy(alpha = 0.6f), InnerShape)
                .clickable(enabled = false) {}
        ) {
            ItemHeader(item = item, level = level, rarity = rarity)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Section(
                    title = "Stats",
                    badge = { PowerBadge(power = power, delta = powerDelta) }
                ) {
                    if (rows.isEmpty()) {
                        EmptyLine("This piece carries no raw stats.")
                    } else {
                        rows.forEachIndexed { index, row ->
                            if (index > 0) Hairline()
                            StatRow(row)
                        }
                    }
                }

                Section(title = "Abilities") {
                    if (item.description.isNotBlank()) {
                        Text(
                            text = item.description,
                            color = ParchmentPalette.InkMuted,
                            fontFamily = FontFirst(),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                    if (item.effects.isEmpty() && item.description.isBlank()) {
                        EmptyLine("No special abilities.")
                    }
                    // Zdolnosci sa wizualnie najmocniejsza rzecza w tej sekcji - to one sa
                    // powodem, dla ktorego gracz zaklada statystycznie slabszy przedmiot.
                    item.effects.forEach { effect ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "✦", color = AbilityGold, fontSize = 13.sp)
                            Text(
                                text = effect.description,
                                color = ParchmentPalette.Ink,
                                fontFamily = FontFirst(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                if (socketCount > 0) {
                    Section(title = "Sockets") {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            repeat(socketCount) { index ->
                                val jewel = socketed.getOrNull(index)
                                if (jewel != null) {
                                    JewelIcon(
                                        jewel = jewel,
                                        size = 46.dp,
                                        modifier = Modifier.clickable { onSocketClick(index) }
                                    )
                                } else {
                                    EmptySocket(size = 46.dp, modifier = Modifier.clickable { onSocketClick(null) })
                                }
                            }
                        }
                        socketed.forEach { jewel ->
                            Text(
                                text = "${jewel.name}: ${jewel.bonusText}",
                                color = ParchmentPalette.Ink,
                                fontFamily = FontFirst(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        Text(
                            text = if (socketed.isEmpty()) "Tap an empty socket to set a jewel." else "Tap a jewel to take it out - it goes back to your pouch.",
                            color = ParchmentPalette.InkMuted,
                            fontFamily = FontFirst(),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                UpgradeTrack(level = level)

                comparedTo?.let { current ->
                    Text(
                        text = "Compared with equipped: ${current.name}",
                        color = ParchmentPalette.InkMuted,
                        fontFamily = FontFirst(),
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (equipped) {
                    ParchmentButton(
                        text = "UNEQUIP",
                        color = UnequipRed,
                        onClick = onUnequip,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    ParchmentButton(
                        text = "EQUIP",
                        onClick = onEquip,
                        modifier = Modifier.weight(1f)
                    )
                }
                ParchmentButton(
                    text = "CLOSE",
                    color = ParchmentPalette.Inset,
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Gora karty: ikona z poziomem, plakietka rzadkosci i dwie linijki "czym to jest".
 *
 * Siedzi na JASNIEJSZYM pasku niz reszta papieru - dzieki temu czyta sie jak naglowek, a nie jak
 * pierwszy wiersz tresci, i nie trzeba do tego ani jednej dodatkowej kreski.
 */
@Composable
private fun ItemHeader(item: Item, level: Int, rarity: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ParchmentPalette.Card)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ItemIcon(item = item, level = level, rarity = rarity, size = 84.dp)

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Plakietka rzadkosci jako ciemna pigulka: kolor rzadkosci na kremowym papierze
            // sam w sobie bylby za jasny, zeby cokolwiek krzyczec.
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(rarity.darken(0.55f))
                    .border(1.5.dp, rarity, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 5.dp)
            ) {
                Text(
                    text = RarityPalette.label(item.rarity),
                    color = Color.White,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            Text(
                text = "Lv. ${level.coerceAtLeast(ItemUpgrades.MIN_LEVEL)}/${ItemUpgrades.MAX_LEVEL}",
                color = ParchmentPalette.Ink,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = "Type: ${item.slot.label()}",
                color = ParchmentPalette.Ink,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

/**
 * Kafel z ikona przedmiotu i plakietka poziomu w rogu.
 *
 * Wspoldzielony z ekranem postaci i plecakiem, bo to WLASNIE ten kafel niesie cala tozsamosc
 * przedmiotu: kolor ramki = rzadkosc, liczba w rogu = ile w niego wlozono.
 */
@Composable
fun ItemIcon(
    item: Item,
    level: Int,
    rarity: Color,
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.17f))
            .background(
                Brush.verticalGradient(
                    listOf(rarity.copy(alpha = 0.45f), rarity.copy(alpha = 0.18f))
                )
            )
            .border(3.dp, rarity, RoundedCornerShape(size * 0.17f)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = item.icon(), fontSize = (size.value * 0.46f).sp)

        if (level > ItemUpgrades.MIN_LEVEL) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(3.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(ParchmentPalette.Frame)
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "Lv.$level",
                    color = Color.White,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.13f).coerceIn(8f, 12f).sp
                )
            }
        }
    }
}

/**
 * Sekcja karty: belka tytulowa w ciemniejszym tanie, tresc na jasnej karcie pod nia.
 *
 * Belka jest tu wazniejsza, niz wyglada: bez niej "Statystyki" i "Zdolnosci" to byly dwa akapity
 * tego samego tekstu i oko nie mialo sie o co zaczepic przy skakaniu po karcie.
 */
@Composable
private fun Section(
    title: String,
    badge: @Composable (() -> Unit)? = null,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ParchmentPalette.Card)
            .border(1.5.dp, ParchmentPalette.CardEdge, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ParchmentPalette.Inset)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = ParchmentPalette.Ink,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
            badge?.invoke()
        }
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            content = content
        )
    }
}

/**
 * Odznaka mocy - jedna liczba, po ktorej porownuje sie przedmioty bez czytania tabelki.
 * Strzalka z roznica pokazuje sie tylko wtedy, gdy jest z czym porownywac.
 */
@Composable
private fun PowerBadge(power: Int, delta: Int?) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(PowerNavy)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "🛡", fontSize = 13.sp)
        Text(
            text = "$power",
            color = Color.White,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        if (delta != null && delta != 0) {
            Text(
                text = if (delta > 0) "▲$delta" else "▼${-delta}",
                color = if (delta > 0) Better else Worse,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
    }
}

/**
 * Tor ulepszenia: ile z dziesieciu poziomow juz w tym siedzi i ile to daje.
 *
 * Bez niego "Lv. 3/10" z naglowka bylo sama liczba - nie dalo sie zobaczyc, ze to JUZ +36%
 * statystyk ani ile jeszcze zostalo do konca toru.
 */
@Composable
private fun UpgradeTrack(level: Int) {
    val safe = level.coerceIn(ItemUpgrades.MIN_LEVEL, ItemUpgrades.MAX_LEVEL)
    val bonus = ((ItemUpgrades.multiplierFor(safe) - 1f) * 100).toInt()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ParchmentPalette.Card)
            .border(1.5.dp, ParchmentPalette.CardEdge, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Upgrade",
                color = ParchmentPalette.Ink,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (bonus > 0) "+$bonus% stats" else "not upgraded yet",
                color = if (bonus > 0) Better else ParchmentPalette.InkMuted,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(ParchmentPalette.Inset)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(safe.toFloat() / ItemUpgrades.MAX_LEVEL)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(50))
                    .background(ParchmentPalette.Button)
            )
        }
    }
}

@Composable
private fun Hairline() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(ParchmentPalette.CardStripe)
    )
}

@Composable
private fun EmptyLine(text: String) {
    Text(
        text = text,
        color = ParchmentPalette.InkMuted,
        fontFamily = FontFirst(),
        fontStyle = FontStyle.Italic,
        fontSize = 12.sp
    )
}

/** Jedna linijka statystyki: ile daje ten przedmiot i o ile to lepiej/gorzej niz obecny. */
private data class StatRow(
    val label: String,
    val value: String,
    val delta: Float,
    val deltaLabel: String?
)

private fun statRows(item: Item, comparedTo: Item?): List<StatRow> =
    STAT_ORDER.mapNotNull { type ->
        val mine = item.stats[type]
        val theirs = comparedTo?.stats?.get(type) ?: 0f
        if (mine == 0f && theirs == 0f) return@mapNotNull null

        val delta = mine - theirs
        StatRow(
            label = type.label(),
            value = (if (mine > 0f) "+" else "") + type.format(mine),
            delta = delta,
            deltaLabel = if (comparedTo != null && delta != 0f) {
                (if (delta > 0) "+" else "") + type.format(delta)
            } else {
                null
            }
        )
    }

@Composable
private fun StatRow(row: StatRow) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = row.label,
            color = ParchmentPalette.Ink,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.size(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            // Zielen na kremowym papierze to jedyny kolor, ktory niesie tu znaczenie: to, co
            // przedmiot DODAJE. Reszta karty jest brazowa wlasnie po to, zeby te liczby widac.
            Text(
                text = row.value,
                color = Better,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            row.deltaLabel?.let { label ->
                Text(
                    text = label,
                    color = if (row.delta > 0) Better else Worse,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

/** Przyciemniony wariant koloru - pod ciemne pigulki na jasnym papierze. */
internal fun Color.darken(factor: Float): Color =
    Color(red = red * (1f - factor), green = green * (1f - factor), blue = blue * (1f - factor), alpha = alpha)

private val CardShape = RoundedCornerShape(20.dp)
private val InnerShape = RoundedCornerShape(16.dp)

internal val Better = Color(0xFF3F8F3F)
internal val Worse = Color(0xFFB03A2E)
internal val PowerNavy = Color(0xFF2E4258)
private val AbilityGold = Color(0xFFC98A1B)
private val UnequipRed = Color(0xFFE08A74)

/* ---- Wspolne dla karty przedmiotu i panelu statystyk postaci ---- */

internal val STAT_ORDER = listOf(
    StatType.MaxHp, StatType.Attack, StatType.Defense, StatType.Speed,
    StatType.CritChance, StatType.CritDamage, StatType.Lifesteal, StatType.Dodge,
    StatType.DamageReduction, StatType.SpellPower, StatType.HealPower,
    StatType.CooldownReduction, StatType.AttackTargets
)

internal fun StatType.label(): String = when (this) {
    StatType.MaxHp -> "Health"
    StatType.Attack -> "Attack"
    StatType.Defense -> "Defense"
    StatType.Speed -> "Speed"
    StatType.CritChance -> "Crit Chance"
    StatType.CritDamage -> "Crit Damage"
    StatType.Dodge -> "Dodge"
    StatType.Lifesteal -> "Lifesteal"
    StatType.DamageReduction -> "Damage Reduction"
    StatType.HealPower -> "Heal Power"
    StatType.CooldownReduction -> "Cooldown Reduction"
    StatType.AttackTargets -> "Extra Targets"
    StatType.SpellPower -> "Spell Power"
}

internal fun StatType.isPercent(): Boolean = this in setOf(
    StatType.CritChance, StatType.CritDamage, StatType.Dodge, StatType.Lifesteal,
    StatType.DamageReduction, StatType.HealPower, StatType.CooldownReduction, StatType.SpellPower
)

internal fun StatType.format(value: Float): String =
    if (isPercent()) "${(value * 100).toInt()}%" else value.toInt().toString()
