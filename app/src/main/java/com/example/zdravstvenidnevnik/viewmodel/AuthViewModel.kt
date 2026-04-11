package com.example.zdravstvenidnevnik.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthUiState(
    val currentUser: FirebaseUser? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel : ViewModel() {
    private val firebaseAuth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(
        AuthUiState(currentUser = firebaseAuth.currentUser)
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        _uiState.update {
            it.copy(
                currentUser = auth.currentUser,
                isLoading = false
            )
        }
    }

    init {
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Email and password are required.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                firebaseAuth.signInWithEmailAndPassword(email, password).await()
                _uiState.update {
                    it.copy(
                        currentUser = firebaseAuth.currentUser,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Authentication failed."
                    )
                }
            }
        }
    }

    fun register(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Email and password are required.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                _uiState.update {
                    it.copy(
                        currentUser = firebaseAuth.currentUser,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Registration failed."
                    )
                }
            }
        }
    }

    fun updateDisplayName(displayName: String) {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            _uiState.update { it.copy(errorMessage = "User is not signed in.") }
            return
        }
        if (displayName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Display name is required.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val profileUpdates = userProfileChangeRequest {
                    this.displayName = displayName
                }
                currentUser.updateProfile(profileUpdates).await()
                currentUser.reload().await()
                _uiState.update {
                    it.copy(
                        currentUser = firebaseAuth.currentUser,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Display name update failed."
                    )
                }
            }
        }
    }

    fun logout() {
        firebaseAuth.signOut()
        _uiState.value = AuthUiState(currentUser = null)
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    override fun onCleared() {
        firebaseAuth.removeAuthStateListener(authStateListener)
        super.onCleared()
    }
}
