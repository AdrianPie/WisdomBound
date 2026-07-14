package com.lernhero.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lernhero.domain.data.PlayerRepository
import com.lernhero.domain.preset.Character
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthViewModel(
    private val playerRepository: PlayerRepository
): ViewModel() {

    private val _uiState = MutableStateFlow(AuthStateUi())
    val uiState: StateFlow<AuthStateUi> = _uiState

    private val _user = MutableStateFlow<FirebaseUser?>(null)
    val user = _user.asStateFlow()


    fun toggleAnimate() {
        _uiState.value = _uiState.value.copy(animate = !uiState.value.animate)
    }

    fun setUser(newUser: FirebaseUser?) {
        _user.value = newUser
    }

    fun currentFirebaseUser(): FirebaseUser? = Firebase.auth.currentUser

    /** Checks whether the signed-in user already has a character saved in Firestore. */
    fun checkHasCharacter(onResult: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val hasCharacter = playerRepository.getPlayer()?.character != null
            withContext(Dispatchers.Main) {
                onResult(hasCharacter)
            }
        }
    }


    fun createPlayer(
        user: FirebaseUser?,
        character: Character,
        name: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ){
        viewModelScope.launch(Dispatchers.IO) {
            playerRepository.createPlayer(
                user = user,
                onSuccess = onSuccess,
                name = name,
                onFailure = onFailure,
                character = character
            )
        }
    }

}
