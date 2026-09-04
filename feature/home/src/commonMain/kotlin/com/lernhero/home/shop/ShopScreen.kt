package com.lernhero.home.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.home.player.PlayerViewModel
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel

private val PanelInk = Color(0xFF19120C)
private val Frame = Color(0xFFC8A45A)
private val Cream = Color(0xFFF4E9D7)
private val Muted = Color(0xFF9D8B6A)
private val GoldText = Color(0xFFFFD34D)
private val GemBlue = Color(0xFF6FB7FF)

/**
 * Pakiet do kupienia za prawdziwe pieniadze. Na razie bez platnosci - ekran ma pokazac, co bedzie
 * w sklepie, i (co wazniejsze) czego w nim NIE bedzie.
 */
private data class GemPack(
    val id: String,
    val title: String,
    val gems: Int,
    val bonus: String?,
    val price: String,
    val emoji: String
)

private val GEM_PACKS = listOf(
    GemPack("small", "Pouch", 100, null, "$9.99", "💎"),
    GemPack("medium", "Chest", 550, "+10% free", "$39.99", "💎"),
    GemPack("large", "Vault", 1500, "+25% free", "$99.99", "💎")
)

/**
 * Sklep za prawdziwa walute. Ekwipunek, kompani i zaklecia ZNIKLY stad swiadomie: kupowanie ich za
 * zloto bylo obejsciem calej rozgrywki (nie musiales schodzic do lochu, zeby sie wzmocnic), a
 * jednoczesnie zabijalo ekonomie - po wykupieniu katalogu zloto przestawalo miec sens.
 *
 * Teraz przedmioty wykuwa sie w Kuzni z rudy, a kompanow i zaklecia przyzywa w Summon Center za
 * esencje i losy. Jedno i drugie zdobywa sie WYLACZNIE w lochach, wiec kazda nagroda z wyprawy ma
 * gdzie trafic. Tutaj zostaje tylko to, co uczciwie moze byc za prawdziwe pieniadze.
 */
@Composable
fun ShopScreen(viewModel: PlayerViewModel) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Header(money = state.money)

        Text(
            text = "KLEJNOTY",
            color = GoldText,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )

        GEM_PACKS.forEach { pack ->
            GemPackCard(pack = pack)
        }

        WhereToGetThings()
    }
}

@Composable
private fun Header(money: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PanelInk)
            .border(2.dp, Frame, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "SKLEP",
                color = GoldText,
                fontFamily = FontPixel(),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = "Gems for real money",
                color = Muted,
                fontFamily = FontFirst(),
                fontSize = 11.sp
            )
        }
        Text(
            text = "💰 $money",
            color = Cream,
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}

@Composable
private fun GemPackCard(pack: GemPack) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF1B2430), PanelInk)))
            .border(1.5.dp, GemBlue.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = pack.emoji, fontSize = 30.sp)
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = pack.title,
                    color = Cream,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF2A3340))
                        .border(1.dp, GemBlue.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .clickable { }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = pack.price,
                        color = Muted,
                        fontFamily = FontFirst(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
            Text(
                text = buildString {
                    append("${pack.gems} klejnotow")
                    pack.bonus?.let { append(" · $it") }
                },
                color = GemBlue,
                fontFamily = FontFirst(),
                fontSize = 12.sp
            )
            Text(
                text = "Payments aren't live yet",
                color = Muted.copy(alpha = 0.7f),
                fontFamily = FontFirst(),
                fontSize = 10.sp
            )
        }
    }
}

/**
 * Najwazniejszy element tego ekranu: mowi graczowi, gdzie sie TERAZ zdobywa rzeczy. Bez tego
 * usuniecie zakladek z ekwipunkiem wygladalo by na zepsuta gre, a nie na zmiane projektu.
 */
@Composable
private fun WhereToGetThings() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PanelInk)
            .border(1.5.dp, Frame.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "SPRZETU NIE KUPUJESZ",
            color = GoldText,
            fontFamily = FontPixel(),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
        Text(
            text = "Everything is crafted at Home from what you bring back from the dungeon:",
            color = Muted,
            fontFamily = FontFirst(),
            fontSize = 12.sp
        )
        SourceRow("⚒", "Forge", "Items — for ore, wood and stone.")
        SourceRow("🔮", "Summon Center", "Companions for tickets, spells for essence.")
        SourceRow("⚔", "Dungeons", "Ore and essence drop from waves and bosses.")
        Text(
            text = "Gems will never be a shortcut to power - only to convenience.",
            color = Muted.copy(alpha = 0.75f),
            fontFamily = FontFirst(),
            fontSize = 11.sp,
            textAlign = TextAlign.Start
        )
    }
}

@Composable
private fun SourceRow(icon: String, title: String, description: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(text = icon, fontSize = 16.sp)
        Column {
            Text(
                text = title,
                color = Cream,
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(
                text = description,
                color = Muted,
                fontFamily = FontFirst(),
                fontSize = 11.sp
            )
        }
    }
}
