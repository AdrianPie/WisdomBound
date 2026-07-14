package com.lernhero.auth

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lernhero.domain.preset.Character
import com.lernhero.domain.preset.CharacterPreset
import com.lernhero.shared.ButtonPrimary
import com.lernhero.shared.FontFirst
import com.lernhero.shared.FontPixel
import com.lernhero.shared.FontSize
import com.lernhero.shared.Resources
import com.lernhero.shared.Surface
import com.lernhero.shared.TextPrimary
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

private val PanelColor = Color(0xFF221B15)
private val PanelBorder = Color(0xFF6B5A38)
private val AccentGold = Color(0xFFC8A45A)

@Composable
fun CharacterCreationScreen(
    navigateToHome: () -> Unit
) {
    val viewModel = koinViewModel<AuthViewModel>()

    var name by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf<Character?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Stworz postac",
            color = AccentGold,
            fontFamily = FontPixel(),
            fontSize = FontSize.LARGE
        )
        Text(
            text = "Wybierz imie i klase swojego bohatera",
            color = TextPrimary,
            fontFamily = FontFirst(),
            style = MaterialTheme.typography.bodyMedium
        )

        OutlinedTextField(
            value = name,
            onValueChange = { newValue ->
                if (newValue.length <= 16) name = newValue
            },
            singleLine = true,
            label = {
                Text(
                    text = "Imie bohatera",
                    fontFamily = FontFirst()
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = AccentGold,
                unfocusedBorderColor = PanelBorder,
                focusedLabelColor = AccentGold,
                unfocusedLabelColor = TextPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ClassCard(
                title = "Rycerz",
                avatar = Resources.Image.knightAvatar,
                character = CharacterPreset.Knight,
                selected = selectedClass?.id == CharacterPreset.Knight.id,
                onClick = { selectedClass = CharacterPreset.Knight },
                modifier = Modifier.weight(1f)
            )
            ClassCard(
                title = "Czarodziej",
                avatar = Resources.Image.sorcererAvatar,
                character = CharacterPreset.Sorcerer,
                selected = selectedClass?.id == CharacterPreset.Sorcerer.id,
                onClick = { selectedClass = CharacterPreset.Sorcerer },
                modifier = Modifier.weight(1f)
            )
        }

        errorMessage?.let { message ->
            Text(
                text = message,
                color = Color(0xFFE04736),
                fontFamily = FontFirst(),
                style = MaterialTheme.typography.bodySmall
            )
        }

        Button(
            onClick = {
                val chosen = selectedClass ?: return@Button
                isSaving = true
                errorMessage = null
                viewModel.createPlayer(
                    user = viewModel.currentFirebaseUser(),
                    character = chosen,
                    name = name.trim(),
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
            enabled = !isSaving && name.isNotBlank() && selectedClass != null,
            colors = ButtonDefaults.buttonColors(
                containerColor = ButtonPrimary,
                contentColor = TextPrimary,
                disabledContainerColor = PanelColor,
                disabledContentColor = PanelBorder
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = TextPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Rozpocznij przygode",
                    fontFamily = FontFirst()
                )
            }
        }
    }
}

@Composable
private fun ClassCard(
    title: String,
    avatar: DrawableResource,
    character: Character,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) AccentGold else PanelBorder
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.03f else 1f
    )

    Column(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(10.dp))
            .background(PanelColor)
            .border(2.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, borderColor, RoundedCornerShape(8.dp))
        ) {
            Image(
                painter = painterResource(avatar),
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Text(
            text = title,
            color = if (selected) AccentGold else TextPrimary,
            fontFamily = FontPixel(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        StatBar(label = "HP", value = character.maxHp, max = 60, color = Color(0xFFE04736))
        StatBar(label = "Atak", value = character.attack, max = 60, color = Color(0xFFF97316))
    }
}

@Composable
private fun StatBar(
    label: String,
    value: Int,
    max: Int,
    color: Color
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = TextPrimary,
                fontFamily = FontFirst(),
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = value.toString(),
                color = color,
                fontFamily = FontFirst(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF120E0A))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((value.toFloat() / max).coerceIn(0f, 1f))
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(color)
            )
        }
        Spacer(modifier = Modifier.width(0.dp))
    }
}
