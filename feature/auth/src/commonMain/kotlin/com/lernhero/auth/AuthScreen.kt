package com.lernhero.auth

import ContentWithMessageBar
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lernhero.auth.compontent.GoogleButton
import com.lernhero.shared.FontFirst
import com.lernhero.shared.FontSize
import com.lernhero.shared.Resources
import com.lernhero.shared.Surface
import com.lernhero.shared.TextPrimary
import com.mmk.kmpauth.firebase.google.GoogleButtonUiContainerFirebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lernhero.feature.auth.generated.resources.Res
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import rememberMessageBarState

@Composable
fun AuthScreen(
    navigateToHome: () -> Unit,
    navigateToCharacterCreation: () -> Unit
) {
    val viewModel = koinViewModel<AuthViewModel>()
    val messageBarState = rememberMessageBarState()
    var loadingState by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Surface
    ) {
        padding ->
        ContentWithMessageBar(
            contentBackgroundColor = Surface,
            modifier = Modifier
                .padding(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding(),
                ),
            messageBarState = messageBarState,
            errorMaxLines = 2

        ){
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),) {
                Column(modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Image(
                      painter = painterResource(Resources.Image.logo),
                      contentDescription = "dupa"
                  )
                    Text(modifier = Modifier
                            .fillMaxWidth()
                            .alpha(0.5f),
                        textAlign = TextAlign.Center,
                        fontFamily = FontFirst(),
                        fontSize = FontSize.EXTRA_REGULAR,
                        color = TextPrimary,
                        text = "Sign in to continue"
                    )


                }
                GoogleButtonUiContainerFirebase(
                    linkAccount = false,
                    onResult = { result ->
                        result.onSuccess { user ->
                            viewModel.setUser(user)
                            // Gracz z postacia idzie od razu do Home,
                            // nowy gracz najpierw tworzy postac.
                            viewModel.checkHasCharacter { hasCharacter ->
                                loadingState = false
                                if (hasCharacter) {
                                    navigateToHome()
                                } else {
                                    navigateToCharacterCreation()
                                }
                            }
                        }.onFailure { error ->
                            if (error.message?.contains("a network error") == true) {
                                messageBarState.addError("Internet connection not available")
                            } else if (error.message?.contains("Idtoken is null") == true) {
                                messageBarState.addError("Google Play Services not available")
                            } else {
                                messageBarState.addError("Sign in failed: ${error.message}")
                            }
                            loadingState = false
                        }
                    }

                ){
                    GoogleButton(
                        loading = loadingState,
                        onClick = {
                            loadingState = true
                            this@GoogleButtonUiContainerFirebase.onClick()
                        },
                    )
                }
            }
        }
    }
}
