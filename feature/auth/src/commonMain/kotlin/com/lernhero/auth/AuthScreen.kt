package com.lernhero.auth

import ContentWithMessageBar
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lernhero.auth.component.AuthBackdrop
import com.lernhero.auth.component.ButtonTone
import com.lernhero.auth.component.FloatingEmblem
import com.lernhero.auth.component.GameButton
import com.lernhero.auth.component.GamePanel
import com.lernhero.auth.component.GameText
import com.lernhero.auth.component.GameTextField
import com.lernhero.auth.component.GoogleSignInButton
import com.lernhero.auth.component.LabeledDivider
import com.lernhero.shared.i18n.AppStrings
import com.lernhero.shared.i18n.strings
import com.lernhero.shared.theme.AppPalette
import com.lernhero.shared.theme.FontFirst
import com.lernhero.shared.theme.FontPixel
import com.mmk.kmpauth.firebase.apple.AppleButtonUiContainer
import com.mmk.kmpauth.firebase.google.GoogleButtonUiContainerFirebase
import dev.gitlive.firebase.auth.FirebaseUser
import org.koin.compose.viewmodel.koinViewModel
import rememberMessageBarState

/**
 * Logowanie: trzy drogi do tego samego konta Firebase - Google, Apple (tylko iOS) i zwykly e-mail
 * z haslem. Kazda konczy sie tym samym pytaniem: czy ten uzytkownik ma juz postac? Jesli tak,
 * idzie na Dom; jesli nie - na tworzenie postaci.
 *
 * Logowanie i zakladanie konta to dwa tryby tej samej karty, przelaczane zakladkami na jej gorze.
 * Wczesniej zmienial sie tylko napis na przycisku i gracz nie zauwazal, ze jest w innym trybie -
 * teraz tryb ma wlasny naglowek, dodatkowe pole na powtorzenie hasla i inny kolor przycisku.
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

    var emailLoading by remember { mutableStateOf(false) }
    var providerLoading by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }
    var registerMode by remember { mutableStateOf(false) }
    val busy = emailLoading || providerLoading

    // Jedno miejsce, w ktorym konczy sie KAZDY sposob logowania - inaczej latwo o sciezke, ktora
    // wpuszcza gracza bez postaci prosto do gry.
    fun onSignedIn(user: FirebaseUser?) {
        viewModel.setUser(user)
        viewModel.checkHasCharacter { hasCharacter ->
            emailLoading = false
            providerLoading = false
            if (hasCharacter) navigateToHome() else navigateToCharacterCreation()
        }
    }

    fun onFailure(failure: AuthFailure) {
        emailLoading = false
        providerLoading = false
        messageBarState.addError(failure.message(t))
    }

    fun onProviderError(error: Throwable?) {
        providerLoading = false
        val message = error?.message.orEmpty()
        messageBarState.addError(
            when {
                message.contains("network error", ignoreCase = true) -> t.authErrorNoNetwork
                message.contains("Idtoken is null") -> t.authErrorPlayServices
                message.isBlank() -> t.authErrorGeneric("?")
                else -> t.authErrorGeneric(message)
            }
        )
    }

    fun submit() {
        if (busy) return
        if (email.isBlank() || password.isBlank()) {
            messageBarState.addError(t.authErrorEmptyFields)
            return
        }
        if (registerMode) {
            // Firebase i tak odrzuci krotsze haslo - lepiej powiedziec to od razu, niz po podrozy
            // w obie strony przez siec.
            if (password.length < 6) {
                messageBarState.addError(t.authErrorShortPassword)
                return
            }
            if (password != repeatPassword) {
                messageBarState.addError(t.authErrorPasswordsDiffer)
                return
            }
        }
        emailLoading = true
        if (registerMode) {
            viewModel.registerWithEmail(email, password, ::onSignedIn, ::onFailure)
        } else {
            viewModel.signInWithEmail(email, password, ::onSignedIn, ::onFailure)
        }
    }

    fun resetPassword() {
        if (email.isBlank()) {
            messageBarState.addError(t.authResetNeedEmail)
            return
        }
        val target = email.trim()
        viewModel.sendPasswordReset(target) { failure ->
            if (failure == null) messageBarState.addSuccess(t.authResetSent(target))
            else messageBarState.addError(failure.message(t))
        }
    }

    AuthBackdrop {
        ContentWithMessageBar(
            contentBackgroundColor = Color.Transparent,
            modifier = Modifier.systemBarsPadding(),
            messageBarState = messageBarState,
            errorMaxLines = 3
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                FloatingEmblem(size = 150.dp)
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    GameText(text = "WisdomBound", size = 38.sp, color = Color(0xFFFFD34D), strokeWidth = 8f)
                    Text(
                        text = t.authTagline,
                        textAlign = TextAlign.Center,
                        fontFamily = FontPixel(),
                        fontSize = 15.sp,
                        color = Color(0xFFF4E3B8)
                    )
                }

                GamePanel(modifier = Modifier.widthIn(max = 440.dp).fillMaxWidth().animateContentSize()) {
                    ModeSwitch(
                        registerMode = registerMode,
                        signInLabel = t.authSignIn,
                        registerLabel = t.authCreateAccount,
                        onChange = { if (!busy) registerMode = it }
                    )

                    AnimatedContent(
                        targetState = registerMode,
                        transitionSpec = { fadeIn() togetherWith fadeOut() }
                    ) { register ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            GameText(
                                text = if (register) t.authWelcomeNew else t.authWelcomeBack,
                                size = 22.sp,
                                color = Color.White
                            )
                            Text(
                                text = if (register) t.authWelcomeNewHint else t.authWelcomeBackHint,
                                color = Color(0xFFCDB9E6),
                                fontFamily = FontFirst(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    GameTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = t.authEmail,
                        icon = "✉️",
                        keyboardType = KeyboardType.Email
                    )
                    GameTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = t.authPassword,
                        icon = "🔒",
                        password = true,
                        imeAction = if (registerMode) ImeAction.Next else ImeAction.Done,
                        onImeAction = { if (!registerMode) submit() }
                    )

                    AnimatedVisibility(
                        visible = registerMode,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            GameTextField(
                                value = repeatPassword,
                                onValueChange = { repeatPassword = it },
                                placeholder = t.authConfirmPassword,
                                icon = "🔁",
                                password = true,
                                imeAction = ImeAction.Done,
                                onImeAction = ::submit
                            )
                            Text(
                                text = t.authPasswordRule,
                                color = Color(0xFFBFA878),
                                fontFamily = FontFirst(),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }

                    AnimatedVisibility(visible = !registerMode) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                            Text(
                                text = t.authForgotPassword,
                                color = Color(0xFFFFD34D),
                                fontFamily = FontFirst(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = ::resetPassword
                                )
                            )
                        }
                    }

                    GameButton(
                        text = if (registerMode) t.authCreateAccount else t.authSignIn,
                        onClick = ::submit,
                        tone = if (registerMode) ButtonTone.Green else ButtonTone.Gold,
                        loading = emailLoading,
                        enabled = !providerLoading,
                        modifier = Modifier.fillMaxWidth()
                    )

                    LabeledDivider(label = t.authOr)

                    GoogleButtonUiContainerFirebase(
                        linkAccount = false,
                        onResult = { result ->
                            result
                                .onSuccess { user -> onSignedIn(user) }
                                .onFailure { error -> onProviderError(error) }
                        }
                    ) {
                        GoogleSignInButton(
                            text = if (providerLoading) t.authGoogleWaiting else t.authContinueWithGoogle,
                            loading = providerLoading,
                            onClick = {
                                if (busy) return@GoogleSignInButton
                                providerLoading = true
                                this@GoogleButtonUiContainerFirebase.onClick()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Apple pokazujemy TYLKO na iOS - na Androidzie kmpauth nie ma tego przeplywu,
                    // a martwy przycisk logowania jest gorszy niz jego brak.
                    if (supportsAppleSignIn()) {
                        AppleButtonUiContainer(
                            onResult = { result ->
                                result
                                    .onSuccess { user -> onSignedIn(user) }
                                    .onFailure { error -> onProviderError(error) }
                            }
                        ) {
                            GameButton(
                                text = t.authContinueWithApple,
                                onClick = {
                                    if (busy) return@GameButton
                                    providerLoading = true
                                    this@AppleButtonUiContainer.onClick()
                                },
                                tone = ButtonTone.Plain,
                                textColor = Color.Black,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
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
                        fontSize = 12.sp,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = navigateToCharacterCreationPreview
                        )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * Dwie zakladki na gorze karty z przesuwajacym sie zlotym podswietleniem - wyrazny sygnal, ze
 * logowanie i zakladanie konta to dwie rozne rzeczy.
 */
@Composable
private fun ModeSwitch(
    registerMode: Boolean,
    signInLabel: String,
    registerLabel: String,
    onChange: (Boolean) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xCC0D0812))
            .border(1.5.dp, Color(0xFF5A4630), RoundedCornerShape(14.dp))
            .padding(4.dp)
    ) {
        val half = maxWidth / 2
        val offset by animateDpAsState(if (registerMode) half else 0.dp)
        Box(
            modifier = Modifier
                .offset(x = offset)
                .width(half)
                .fillMaxHeight()
                .clip(RoundedCornerShape(11.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFFFFD34D), Color(0xFFE08A1E))))
                .border(2.dp, Color(0xFF120B05), RoundedCornerShape(11.dp))
        )
        Row(modifier = Modifier.fillMaxSize()) {
            listOf(false to signInLabel, true to registerLabel).forEach { (mode, label) ->
                val selected = mode == registerMode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onChange(mode) },
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) {
                        GameText(text = label, size = 15.sp, color = Color.White, strokeWidth = 5f)
                    } else {
                        Text(label, color = Color(0xFFBFA878), fontFamily = FontFirst(), fontWeight = FontWeight.Black, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

private fun AuthFailure.message(t: AppStrings): String = when (this) {
    AuthFailure.WrongCredentials -> t.authErrorWrongCredentials
    AuthFailure.EmailInUse -> t.authErrorEmailInUse
    AuthFailure.InvalidEmail -> t.authErrorInvalidEmail
    AuthFailure.WeakPassword -> t.authErrorWeakPassword
    AuthFailure.Network -> t.authErrorNoNetwork
    AuthFailure.TooManyAttempts -> t.authErrorTooManyAttempts
    is AuthFailure.Other -> t.authErrorGeneric(message)
}
