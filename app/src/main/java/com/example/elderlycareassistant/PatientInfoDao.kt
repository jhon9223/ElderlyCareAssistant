package com.example.elderlycareassistant

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientInfoDao {
    @Insert
    suspend fun insert(patientInfo: PatientInfoEntity)

    @Query("SELECT * FROM patient_info")
    fun getAllPatientInfo(): Flow<List<PatientInfoEntity>>

    @Delete
    suspend fun delete(patientInfo: PatientInfoEntity)
}