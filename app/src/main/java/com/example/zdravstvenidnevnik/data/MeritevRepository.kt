package com.example.zdravstvenidnevnik.data

import kotlinx.coroutines.flow.Flow

class MeritevRepository(private val meritevDao: MeritevDao) {

    val vseMeritve: Flow<List<Meritev>> = meritevDao.getAll()

    fun getMeritveByUser(userId: String): Flow<List<Meritev>> {
        return meritevDao.getAllByUser(userId)
    }

    suspend fun insert(meritev: Meritev): Long {
        return meritevDao.insert(meritev)
    }

    suspend fun update(meritev: Meritev) {
        meritevDao.update(meritev)
    }

    suspend fun delete(meritev: Meritev) {
        meritevDao.delete(meritev)
    }

    suspend fun deleteAllByUser(userId: String) {
        meritevDao.deleteAllByUser(userId)
    }

    fun getById(id: Int): Flow<Meritev?> {
        return meritevDao.getById(id)
    }
}
