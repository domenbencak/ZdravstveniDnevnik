package com.example.zdravstvenidnevnik.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun insertMeritev(meritev: Meritev) {
        firestore.collection(COLLECTION_MERITVE)
            .add(meritev.toFirestoreMap())
            .await()
    }

    suspend fun fetchAllByUser(userId: String): List<Meritev> {
        return firestore.collection(COLLECTION_MERITVE)
            .whereEqualTo("userId", userId)
            .get()
            .await()
            .documents
            .mapNotNull { document ->
                val data = document.data ?: return@mapNotNull null
                Meritev.fromFirestoreMap(data)
            }
            .sortedByDescending { it.datum }
    }

    private companion object {
        private const val COLLECTION_MERITVE = "meritve"
    }
}
