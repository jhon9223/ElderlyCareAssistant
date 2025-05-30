package com.example.elderlycareassistant

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patient_info")
data class PatientInfoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val weight: String,
    val height: String
)