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
    val temperatura: Double // telesna temperatura (°C)
)