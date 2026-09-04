package com.lernhero.auth

import ContentWithMessageBar
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lernhero.auth.component.GoogleButton
import com.lernhero.shared.i18n.strings
import com.lernhero.shared.resources.Resources
import com.lernhero.shared.theme.AppPalette
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import com.lernhero.shared.theme.FontSize
import com.mmk.kmpauth.firebase.apple.AppleButtonUiContainer
import com.mmk.kmpauth.firebase.google.GoogleButtonUiContainerFirebase
import dev.gitlive.firebase.auth.FirebaseUser
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import rememberMessageBarState

/**
 * Logowanie: trzy drogi do tego samego konta Firebase - Google, Apple (tylko iOS) i zwykly e-mail
 * z haslem. Kazda konczy sie tym samym pytaniem: czy ten uzytkownik ma juz postac? Jesli tak,
 * idzie na Dom; jesli nie - na tworzenie postaci.
 */
@Composable
fun AuthScreen(
    navigateToHome: () -> Unit,
    navigateToCharacterCreation: () -> Unit,
    /** Wejscie deweloperskie prosto na ekran tworzenia postaci - patrz [isDebugBuild]. */
    navigateToCharacterCreationPreview: () -> Unit = {}
) {
    val viewModel = koinViewModel<AuthViewModel>()
    val messageBarState = rememberMessageBarState()
    val t = strings

    var loadingState by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var registerMode by remember { mutableStateOf(false) }

    // Jedno miejsce, w ktorym konczy sie KAZDY sposob logowania - inaczej latwo o sciezke, ktora
    // wpuszcza gracza bez postaci prosto do gry.
    fun onSignedIn(user: FirebaseUser?) {
        viewModel.setUser(user)
        viewModel.checkHasCharacter { hasCharacter ->
            loadingState = false
            if (hasCharacter) navigateToHome() else navigateToCharacterCreation()
        }
    }

    fun onError(error: Throwable?) {
        loadingState = false
        val message = error?.message
        messageBarState.addError(
            when {
                message == null -> t.authErrorGeneric("?")
                message.contains("a network error") -> t.authErrorNoNetwork
                message.contains("Idtoken is null") -> t.authErrorPlayServices
                else -> t.authErrorGeneric(message)
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AppPalette.Background
    ) { padding ->
        ContentWithMessageBar(
            contentBackgroundColor = AppPalette.Background,
            modifier = Modifier.padding(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding()
            ),
            messageBarState = messageBarState,
            errorMaxLines = 2
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppPalette.ScreenGradient)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = painterResource(Resources.Image.logo),
                    contentDescription = "LernHero logo"
                )
                Text(
                    text = t.authTagline,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontFamily = FontPixel(),
                    fontSize = FontSize.EXTRA_REGULAR,
                    color = AppPalette.Gold
                )

                /* ---- e-mail + haslo ---- */
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    singleLine = true,
                    label = { Text(text = t.authEmail, fontFamily = FontFirst()) },
                    colors = authFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    label = { Text(text = t.authPassword, fontFamily = FontFirst()) },
                    colors = authFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            messageBarState.addError(t.authErrorEmptyFields)
                            return@Button
                        }
                        // Firebase i tak odrzuci krotsze haslo - lepiej powiedziec to od razu,
                        // niz po podrozy w obie strony przez siec.
                        if (registerMode && password.length < 6) {
                            messageBarState.addError(t.authErrorShortPassword)
                            return@Button
                        }
                        loadingState = true
                        val onFailure: (String) -> Unit = { onError(Throwable(it)) }
                        if (registerMode) {
                            viewModel.registerWithEmail(email, password, ::onSignedIn, onFailure)
                        } else {
                            viewModel.signInWithEmail(email, password, ::onSignedIn, onFailure)
                        }
                    },
                    enabled = !loadingState,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppPalette.Action,
                        contentColor = AppPalette.GoldBright,
                        disabledContainerColor = AppPalette.Panel,
                        disabledContentColor = AppPalette.TextFaint
                    ),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    if (loadingState) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = AppPalette.GoldBright,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (registerMode) t.authCreateAccount else t.authSignIn,
                            fontFamily = FontPixel(),
                            fontSize = FontSize.EXTRA_REGULAR
                        )
                    }
                }

                Text(
                    text = if (registerMode) t.authHaveAccount else t.authNoAccount,
                    color = AppPalette.TextMuted,
                    fontFamily = FontFirst(),
                    fontSize = FontSize.SMALL,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { registerMode = !registerMode }
                )

                Divider(label = t.authOr)

                /* ---- dostawcy ---- */
                GoogleButtonUiContainerFirebase(
                    linkAccount = false,
                    onResult = { result ->
                        result
                            .onSuccess { user -> onSignedIn(user) }
                            .onFailure { error -> onError(error) }
                    }
                ) {
                    GoogleButton(
                        loading = loadingState,
                        onClick = {
                            loadingState = true
                            this@GoogleButtonUiContainerFirebase.onClick()
                        }
                    )
                }

                // Apple pokazujemy TYLKO na iOS - na Androidzie kmpauth nie ma tego przeplywu,
                // a martwy przycisk logowania jest gorszy niz jego brak.
                if (supportsAppleSignIn()) {
                    AppleButtonUiContainer(
                        onResult = { result ->
                            result
                                .onSuccess { user -> onSignedIn(user) }
                                .onFailure { error -> onError(error) }
                        }
                    ) {
                        ProviderButton(
                            text = t.authContinueWithApple,
                            onClick = {
                                loadingState = true
                                this@AppleButtonUiContainer.onClick()
                            }
                        )
                    }
                }

                // Skrot deweloperski - omija logowanie i wchodzi wprost w tworzenie postaci,
                // zeby dalo sie ogladac ten ekran bez zakladania konta. W buildzie release
                // isDebugBuild() jest false i tego przycisku po prostu nie ma.
                if (isDebugBuild()) {
                    Text(
                        text = "DEV: tworzenie postaci",
                        color = AppPalette.Warning,
                        fontFamily = FontFirst(),
                        fontSize = FontSize.SMALL,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = navigateToCharacterCreationPreview
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun Divider(label: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(Modifier.weight(1f).height(1.dp).background(AppPalette.GoldDim))
        Text(
            text = label,
            color = AppPalette.TextFaint,
            fontFamily = FontFirst(),
            fontSize = FontSize.SMALL
        )
        Box(Modifier.weight(1f).height(1.dp).background(AppPalette.GoldDim))
    }
}

/** Przycisk dostawcy w stylu gry - ten sam ksztalt co przycisk Google obok. */
@Composable
private fun ProviderButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(AppPalette.Ink, RoundedCornerShape(12.dp))
            .border(1.dp, AppPalette.GoldDim, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = AppPalette.TextBody,
            fontFamily = FontFirst(),
            fontSize = FontSize.EXTRA_REGULAR
        )
    }
}

@Composable
private fun authFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = AppPalette.TextBody,
    unfocusedTextColor = AppPalette.TextBody,
    focusedBorderColor = AppPalette.Gold,
    unfocusedBorderColor = AppPalette.GoldDim,
    focusedLabelColor = AppPalette.Gold,
    unfocusedLabelColor = AppPalette.TextMuted,
    cursorColor = AppPalette.Gold
)
