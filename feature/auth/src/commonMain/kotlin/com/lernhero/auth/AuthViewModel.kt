package com.lernhero.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lernhero.domain.repository.PlayerRepository
import com.lernhero.domain.player.HeroAppearance
import com.lernhero.domain.player.HeroClass
import com.lernhero.domain.player.StarterGift
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseNetworkException
import dev.gitlive.firebase.FirebaseTooManyRequestsException
import dev.gitlive.firebase.auth.FirebaseAuthInvalidCredentialsException
import dev.gitlive.firebase.auth.FirebaseAuthInvalidUserException
import dev.gitlive.firebase.auth.FirebaseAuthUserCollisionException
import dev.gitlive.firebase.auth.FirebaseAuthWeakPasswordException
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

    /**
     * Logowanie e-mailem i haslem. Osobna sciezka od Google/Apple: tamte daja gotowego uzytkownika
     * przez UI dostawcy, tutaj konto zaklada i sprawdza sam Firebase Auth.
     */
    fun signInWithEmail(
        email: String,
        password: String,
        onSuccess: (FirebaseUser?) -> Unit,
        onFailure: (AuthFailure) -> Unit
    ) {
        runAuth(onSuccess, onFailure) {
            Firebase.auth.signInWithEmailAndPassword(email.trim(), password).user
        }
    }

    /** Rejestracja e-mailem - konto powstaje, ale postaci jeszcze nie ma (idzie na tworzenie postaci). */
    fun registerWithEmail(
        email: String,
        password: String,
        onSuccess: (FirebaseUser?) -> Unit,
        onFailure: (AuthFailure) -> Unit
    ) {
        runAuth(onSuccess, onFailure) {
            Firebase.auth.createUserWithEmailAndPassword(email.trim(), password).user
        }
    }

    /** Link do zmiany hasla na podany e-mail. Firebase nie zdradza, czy konto istnieje. */
    fun sendPasswordReset(email: String, onDone: (AuthFailure?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = runCatching { Firebase.auth.sendPasswordResetEmail(email.trim()) }
            withContext(Dispatchers.Main) { onDone(result.exceptionOrNull()?.let(AuthFailure::of)) }
        }
    }

    private fun runAuth(
        onSuccess: (FirebaseUser?) -> Unit,
        onFailure: (AuthFailure) -> Unit,
        block: suspend () -> FirebaseUser?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = runCatching { block() }
            withContext(Dispatchers.Main) {
                result
                    .onSuccess { user ->
                        setUser(user)
                        onSuccess(user)
                    }
                    .onFailure { error -> onFailure(AuthFailure.of(error)) }
            }
        }
    }

    fun createPlayer(
        user: FirebaseUser?,
        character: HeroClass,
        name: String,
        appearance: HeroAppearance,
        starterGift: StarterGift?,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ){
        viewModelScope.launch(Dispatchers.IO) {
            playerRepository.createPlayer(
                user = user,
                name = name,
                character = character,
                appearance = appearance,
                starterGift = starterGift,
                // Repozytorium woła te lambdy z watku IO, a z nich idzie nawigacja - stad skok
                // na Main, zamiast liczyc na to, ze Compose to wybaczy.
                onSuccess = { viewModelScope.launch(Dispatchers.Main) { onSuccess() } },
                onFailure = { error -> viewModelScope.launch(Dispatchers.Main) { onFailure(error) } }
            )
        }
    }

}

/**
 * Powod nieudanego logowania. Surowe komunikaty Firebase sa po angielsku i techniczne
 * ("The supplied auth credential is incorrect, malformed or has expired") - ekran pokazuje
 * zamiast nich jedno ludzkie zdanie.
 */
sealed interface AuthFailure {
    data object WrongCredentials : AuthFailure
    data object EmailInUse : AuthFailure
    data object InvalidEmail : AuthFailure
    data object WeakPassword : AuthFailure
    data object Network : AuthFailure
    data object TooManyAttempts : AuthFailure
    data class Other(val message: String) : AuthFailure

    companion object {
        fun of(error: Throwable): AuthFailure = when (error) {
            is FirebaseAuthWeakPasswordException -> WeakPassword
            is FirebaseAuthUserCollisionException -> EmailInUse
            is FirebaseAuthInvalidUserException -> WrongCredentials
            is FirebaseAuthInvalidCredentialsException ->
                if (error.message.orEmpty().contains("email address is badly formatted", ignoreCase = true)) InvalidEmail
                else WrongCredentials
            is FirebaseNetworkException -> Network
            is FirebaseTooManyRequestsException -> TooManyAttempts
            else -> {
                val message = error.message.orEmpty()
                when {
                    message.contains("network error", ignoreCase = true) -> Network
                    else -> Other(message.ifBlank { "?" })
                }
            }
        }
    }
}
