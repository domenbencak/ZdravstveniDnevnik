package com.example.zdravstvenidnevnik.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.zdravstvenidnevnik.data.*
import com.example.zdravstvenidnevnik.network.SensorRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MeritevViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MeritevRepository
    private val firestoreRepository = FirestoreRepository()
    private val sensorRepository = SensorRepository(application.applicationContext)
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val currentUserId = MutableStateFlow(firebaseAuth.currentUser?.uid.orEmpty())

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        currentUserId.value = auth.currentUser?.uid.orEmpty()
    }

    val vseMeritve: StateFlow<List<Meritev>>

    init {
        val dao = MeritevDatabase.getDatabase(application).meritevDao()
        repository = MeritevRepository(dao)
        firebaseAuth.addAuthStateListener(authStateListener)
        vseMeritve = currentUserId
            .flatMapLatest { userId ->
                if (userId.isBlank()) {
                    flowOf(emptyList())
                } else {
                    repository.getMeritveByUser(userId)
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    fun insert(meritev: Meritev, onResult: (Int) -> Unit = {}) {
        viewModelScope.launch {
            val userId = currentUserId.value
            if (userId.isBlank()) {
                Log.w(TAG, "Insert skipped: no authenticated user.")
                onResult(0)
                return@launch
            }

            val meritevWithUser = meritev.copy(userId = userId)
            val localId = repository.insert(meritevWithUser).toInt()
            onResult(localId)

            try {
                firestoreRepository.insertMeritev(
                    meritevWithUser.copy(id = localId)
                )
            } catch (exception: Exception) {
                Log.w(TAG, "Firestore insert failed; local insert kept.", exception)
            }
        }
    }

    fun update(meritev: Meritev, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            val userId = currentUserId.value
            if (userId.isBlank()) {
                Log.w(TAG, "Update skipped: no authenticated user.")
                return@launch
            }
            repository.update(
                meritev.copy(
                    userId = if (meritev.userId.isBlank()) userId else meritev.userId
                )
            )
            onDone()
        }
    }

    fun delete(meritev: Meritev, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.delete(meritev)
            onDone()
        }
    }

    fun syncFromFirestore(onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val userId = currentUserId.value
            if (userId.isBlank()) {
                Log.w(TAG, "Sync skipped: no authenticated user.")
                onComplete(false)
                return@launch
            }

            try {
                val remoteMeritve = firestoreRepository.fetchAllByUser(userId)
                repository.deleteAllByUser(userId)
                remoteMeritve.forEach { meritev ->
                    repository.insert(
                        meritev.copy(
                            id = 0,
                            userId = userId
                        )
                    )
                }
                onComplete(true)
            } catch (exception: Exception) {
                Log.w(TAG, "Firestore sync failed.", exception)
                onComplete(false)
            }
        }
    }

    fun hasHeartRateSensor(): Boolean {
        return sensorRepository.hasHeartRateSensor()
    }

    fun readHeartRate(onResult: (Int?) -> Unit = {}) {
        viewModelScope.launch {
            val sensorValue = sensorRepository.readHeartRateFromSensor()
            if (sensorValue != null) {
                onResult(sensorValue)
                return@launch
            }

            try {
                onResult(sensorRepository.readHeartRateFromApi())
            } catch (exception: Exception) {
                Log.w(TAG, "Heart-rate API read failed.", exception)
                onResult(null)
            }
        }
    }

    fun readSpO2(onResult: (Int?) -> Unit = {}) {
        viewModelScope.launch {
            try {
                onResult(sensorRepository.readSpO2FromApi())
            } catch (exception: Exception) {
                Log.w(TAG, "SpO2 API read failed.", exception)
                onResult(null)
            }
        }
    }

    fun readTemperature(onResult: (Double?) -> Unit = {}) {
        viewModelScope.launch {
            try {
                onResult(sensorRepository.readTemperatureFromApi())
            } catch (exception: Exception) {
                Log.w(TAG, "Temperature API read failed.", exception)
                onResult(null)
            }
        }
    }

    fun getById(id: Int) = repository.getById(id).combine(currentUserId) { meritev, userId ->
        meritev?.takeIf { it.userId == userId }
    }

    override fun onCleared() {
        firebaseAuth.removeAuthStateListener(authStateListener)
        super.onCleared()
    }

    private companion object {
        private const val TAG = "MeritevViewModel"
    }
}
