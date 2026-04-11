package com.example.zdravstvenidnevnik.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MeritevDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(meritev: Meritev): Long
    @Update
    suspend fun update(meritev: Meritev)
    @Delete
    suspend fun delete(meritev: Meritev)

    @Query("SELECT * FROM meritve ORDER BY datum DESC")
    fun getAll(): Flow<List<Meritev>>

    @Query("SELECT * FROM meritve WHERE userId = :userId ORDER BY datum DESC")
    fun getAllByUser(userId: String): Flow<List<Meritev>>

    @Query("DELETE FROM meritve WHERE userId = :userId")
    suspend fun deleteAllByUser(userId: String)

    @Query("SELECT * FROM meritve WHERE id = :id")
    fun getById(id: Int): Flow<Meritev?>
}
