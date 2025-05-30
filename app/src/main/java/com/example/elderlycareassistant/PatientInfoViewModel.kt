package com.example.elderlycareassistant

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class PatientInfo(val id: Int, val weight: String, val height: String)

class PatientInfoViewModel(private val dao: PatientInfoDao) : ViewModel() {
    private val _patientInfoList = mutableStateOf<List<PatientInfo>>(emptyList())
    val patientInfoList: State<List<PatientInfo>> get() = _patientInfoList

    init {
        viewModelScope.launch {
            dao.getAllPatientInfo().collect { entities ->
                _patientInfoList.value = entities.map { PatientInfo(it.id, it.weight, it.height) }
            }
        }
    }

    fun addPatientInfo(weight: String, height: String) {
        if (weight.isNotBlank() && height.isNotBlank()) {
            viewModelScope.launch {
                dao.insert(PatientInfoEntity(weight = weight, height = height))
            }
        }
    }

    fun deletePatientInfo(patientInfo: PatientInfo) {
        viewModelScope.launch {
            dao.delete(PatientInfoEntity(id = patientInfo.id, weight = patientInfo.weight, height = patientInfo.height))
        }
    }
}