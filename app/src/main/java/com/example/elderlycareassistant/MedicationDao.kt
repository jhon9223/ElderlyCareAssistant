package com.example.elderlycareassistant

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MedicationDao {
    @Insert
    suspend fun insert(medication: Medication)

    @Query("SELECT * FROM medications")
    suspend fun getAllMedications(): List<Medication>
}