package com.lernhero.home.city

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.domain.craft.CraftCost
import com.lernhero.domain.craft.Forge
import com.lernhero.domain.craft.ForgeRecipe
import com.lernhero.home.player.PlayerViewModel
import com.lernhero.home.ui.RarityPalette
import com.lernhero.home.ui.icon
import com.lernhero.home.ui.label
import com.lernhero.shared.theme.AppPalette
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel

/**
 * Kuznia - jedyne miejsce, w ktorym powstaje ekwipunek. Wczesniej byla placeholderem: pobierala
 * surowce i NIE dawala nic, a przedmioty kupowalo sie za zloto w sklepie. Teraz jest odwrotnie -
 * sklep nie sprzedaje sprzetu, a kuznia wykuwa prawdziwe przedmioty z [com.lernhero.domain.item.ItemCatalog]
 * za rude przyniesiona z lochu (patrz [Forge.costFor]).
 *
 * Caly koszt i jego sprawdzenie siedzi w domenie ([CraftCost]) i w repozytorium (jeden zapis:
 * odjac surowce, dopisac przedmiot), wiec nie da sie juz stracic materialow bez efektu.
 */
@Composable
fun ForgeScreen(
    playerViewModel: PlayerViewModel,
    onBack: () -> Unit
) {
    val state by playerViewModel.uiState.collectAsState()
    val recipes = remember { Forge.recipes }
    var selectedId by remember { mutableStateOf(recipes.first().id) }
    val recipe = recipes.firstOrNull { it.id == selectedId } ?: recipes.first()

    val owned = recipe.item.id in state.ownedItems
    val affordable = recipe.cost.canAfford(state.resources, state.money)

    TownScreenScaffold(
        title = "Kuznia",
        resources = state.resources,
        money = state.money,
        toasts = emptyList(),
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BlacksmithPanel(recipe = recipe, owned = owned)

            TownPanel(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TownSectionLabel("Co wykuwamy")
                    recipes.forEach { entry ->
                        RecipeRow(
                            recipe = entry,
                            selected = entry.id == selectedId,
                            owned = entry.item.id in state.ownedItems,
                            affordable = entry.cost.canAfford(state.resources, state.money),
                            onClick = { selectedId = entry.id }
                        )
                    }
                }
            }

            CostPanel(cost = recipe.cost, state = state)

            TownActionButton(
                text = if (owned) "JUZ MASZ" else "WYKUJ",
                subtitle = recipe.item.name,
                primary = true,
                enabled = affordable && !owned,
                modifier = Modifier.fillMaxWidth(),
                onClick = { playerViewModel.craftItem(recipe.item, recipe.cost) }
            )

            // Wygaszony przycisk musi powiedziec, DLACZEGO jest wygaszony - inaczej ekran wyglada
            // na zepsuty. Ruda jest tu kluczowa: to jedyny material, ktorego nie produkuje miasto.
            if (!owned && !affordable) {
                val missingIron = recipe.cost.iron > state.resources.iron
                Text(
                    text = if (missingIron) {
                        "Brakuje rudy (masz ${state.resources.iron} z ${recipe.cost.iron}). " +
                            "Ruda wypada tylko w lochach - za kazda oczyszczona fale i za bossow."
                    } else {
                        "Brakuje surowcow - sprawdz rozbicie kosztu powyzej."
                    },
                    color = Color(0xFFD1453D),
                    fontFamily = FontFirst(),
                    fontSize = 11.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            state.message?.let { message ->
                Text(
                    text = message,
                    color = AppPalette.Gold,
                    fontFamily = FontFirst(),
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )
            }
        }
    }
}

/** Podglad wykuwanego przedmiotu - to on jest "wystawa" kuzni zamiast dawnego ozdobnika. */
@Composable
private fun BlacksmithPanel(recipe: ForgeRecipe, owned: Boolean) {
    val item = recipe.item
    val frame = RarityPalette.frame(item.rarity)

    TownPanel(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(RarityPalette.cardBackground(item.rarity))
                    .border(2.dp, frame, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = item.icon(), fontSize = 34.sp)
            }
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = item.name,
                    color = Color(0xFFF4E9D7),
                    fontFamily = FontPixel(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "${item.slot.label()} · ${RarityPalette.label(item.rarity)}",
                    color = frame,
                    fontFamily = FontFirst(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Text(
                    text = item.description,
                    color = Color(0xFF9D8B6A),
                    fontFamily = FontFirst(),
                    fontSize = 11.sp
                )
                if (owned) {
                    Text(
                        text = "Masz juz ten przedmiot",
                        color = AppPalette.Success,
                        fontFamily = FontFirst(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun RecipeRow(
    recipe: ForgeRecipe,
    selected: Boolean,
    owned: Boolean,
    affordable: Boolean,
    onClick: () -> Unit
) {
    val frame = RarityPalette.frame(recipe.item.rarity)
    val border = when {
        selected -> frame
        owned -> AppPalette.Success.copy(alpha = 0.5f)
        else -> Color(0xFF4A3F33)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF15100B).copy(alpha = if (selected) 1f else 0.7f))
            .border(if (selected) 2.dp else 1.dp, border, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = recipe.item.icon(), fontSize = 20.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = recipe.item.name,
                color = if (owned) Color(0xFF7E7364) else Color(0xFFF4E9D7),
                fontFamily = FontFirst(),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(
                text = recipe.cost.entries().joinToString("  ") { (icon, amount) -> "$icon$amount" },
                color = if (affordable) Color(0xFF9D8B6A) else Color(0xFFD1453D),
                fontFamily = FontFirst(),
                fontSize = 11.sp
            )
        }
        if (owned) {
            Text(text = "✓", color = AppPalette.Success, fontFamily = FontPixel(), fontSize = 15.sp)
        }
    }
}

/** Rozbicie kosztu na pozycje - z kolorem mowiacym wprost, czego brakuje. */
@Composable
private fun CostPanel(cost: CraftCost, state: com.lernhero.home.player.PlayerUiState) {
    TownPanel(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TownSectionLabel("Koszt")
            CostLine("🪵", "Drewno", cost.wood, state.resources.wood)
            CostLine("🪨", "Kamien", cost.stone, state.resources.stone)
            CostLine("⚙", "Ruda", cost.iron, state.resources.iron)
            CostLine("💰", "Zloto", cost.gold, state.money)
        }
    }
}

@Composable
private fun CostLine(icon: String, label: String, required: Int, owned: Int) {
    if (required <= 0) return
    val enough = owned >= required

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 13.sp)
            Text(text = label, color = Color(0xFF9D8B6A), fontFamily = FontFirst(), fontSize = 12.sp)
        }
        Text(
            text = "$owned / $required",
            color = if (enough) AppPalette.Success else Color(0xFFD1453D),
            fontFamily = FontFirst(),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}
