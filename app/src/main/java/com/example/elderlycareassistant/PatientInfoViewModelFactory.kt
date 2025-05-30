package com.example.elderlycareassistant

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PatientInfoViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PatientInfoViewModel::class.java)) {
            val dao = AppDatabase.getDatabase(context).patientInfoDao()
            @Suppress("UNCHECKED_CAST")
            return PatientInfoViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}