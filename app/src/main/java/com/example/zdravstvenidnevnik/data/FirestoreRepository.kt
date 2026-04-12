package com.example.zdravstvenidnevnik.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun insertMeritev(meritev: Meritev) {
        firestore.collection(COLLECTION_MERITVE)
            .add(meritev.toFirestoreMap())
            .await()
    }

    suspend fun deleteMeritev(firestoreId: String) {
        firestore.collection(COLLECTION_MERITVE)
            .document(firestoreId)
            .delete()
            .await()
    }

    suspend fun deleteMeritev(meritev: Meritev) {
        if (meritev.userId.isBlank()) return

        val candidates = firestore.collection(COLLECTION_MERITVE)
            .whereEqualTo("userId", meritev.userId)
            .get()
            .await()
            .documents

        val matchingDocumentIds = candidates
            .filter { document ->
                val data = document.data ?: return@filter false
                val datum = (data["datum"] as? Number)?.toLong()
                val srcniUtrip = (data["srcniUtrip"] as? Number)?.toInt()
                val spO2 = (data["spO2"] as? Number)?.toInt()
                val temperatura = (data["temperatura"] as? Number)?.toDouble()

                data["ime"] == meritev.ime &&
                    data["priimek"] == meritev.priimek &&
                    datum == meritev.datum &&
                    srcniUtrip == meritev.srcniUtrip &&
                    spO2 == meritev.spO2 &&
                    temperatura == meritev.temperatura
            }
            .map { it.id }

        matchingDocumentIds.forEach { firestoreId ->
            deleteMeritev(firestoreId)
        }
    }

    fun getMeritveByUser(userId: String): Flow<List<Meritev>> = callbackFlow {
        val listenerRegistration = firestore.collection(COLLECTION_MERITVE)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val meritve = snapshot?.documents
                    ?.mapNotNull { document ->
                        val data = document.data ?: return@mapNotNull null
                        Meritev.fromFirestoreMap(data)
                    }
                    ?.sortedByDescending { it.datum }
                    ?: emptyList()

                trySend(meritve)
            }

        awaitClose { listenerRegistration.remove() }
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
