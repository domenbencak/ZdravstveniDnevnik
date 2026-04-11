package com.example.zdravstvenidnevnik.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meritve")
data class Meritev(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val ime: String,
    val priimek: String,
    val datum: Long, // timestamp v milisekundah
    val srcniUtrip: Int, // udarci na minuto (bpm)
    val spO2: Int, // vsebnost kisika (%)
    val temperatura: Double, // telesna temperatura (°C)
    val userId: String = ""
) {
    fun toFirestoreMap(): Map<String, Any> {
        return mapOf(
            "ime" to ime,
            "priimek" to priimek,
            "datum" to datum,
            "srcniUtrip" to srcniUtrip,
            "spO2" to spO2,
            "temperatura" to temperatura,
            "userId" to userId
        )
    }

    companion object {
        fun fromFirestoreMap(data: Map<String, Any?>): Meritev? {
            val ime = data["ime"] as? String ?: return null
            val priimek = data["priimek"] as? String ?: return null
            val datum = (data["datum"] as? Number)?.toLong() ?: return null
            val srcniUtrip = (data["srcniUtrip"] as? Number)?.toInt() ?: return null
            val spO2 = (data["spO2"] as? Number)?.toInt() ?: return null
            val temperatura = (data["temperatura"] as? Number)?.toDouble() ?: return null
            val userId = data["userId"] as? String ?: ""

            return Meritev(
                ime = ime,
                priimek = priimek,
                datum = datum,
                srcniUtrip = srcniUtrip,
                spO2 = spO2,
                temperatura = temperatura,
                userId = userId
            )
        }
    }
}
