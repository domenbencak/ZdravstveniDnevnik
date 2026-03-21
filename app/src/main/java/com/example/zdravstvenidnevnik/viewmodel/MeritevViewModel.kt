package com.example.zdravstvenidnevnik.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.zdravstvenidnevnik.data.*

class MeritevViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MeritevRepository
    
    val vseMeritve: StateFlow<List<Meritev>>

    init {
        val dao = MeritevDatabase.getDatabase(application).meritevDao()
        repository = MeritevRepository(dao)
        vseMeritve = repository.vseMeritve
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    fun insert(meritev: Meritev, onResult: (Int) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.insert(meritev)
            onResult(id.toInt())
        }
    }

    fun update(meritev: Meritev, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.update(meritev)
            onDone()
        }
    }

    fun delete(meritev: Meritev, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.delete(meritev)
            onDone()
        }
    }

    fun getById(id: Int) = repository.getById(id)
}
