package com.example.elderlycareassistant

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val noteText: String,
    val date: String, // Format: "yyyy-MM-dd" (e.g., "2025-05-06")
    val time: String  // Format: "HH:mm" (e.g., "14:30")
)