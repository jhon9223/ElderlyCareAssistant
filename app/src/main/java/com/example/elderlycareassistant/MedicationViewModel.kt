package com.example.elderlycareassistant

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import java.util.concurrent.TimeUnit

class MedicationViewModel(private val context: Context) : ViewModel() {
    private val _medicationName = mutableStateOf("")
    val medicationName: State<String> get() = _medicationName

    private val _medicationTime = mutableStateOf("")
    val medicationTime: State<String> get() = _medicationTime

    private val _scheduledMedications = mutableStateOf<List<MedSchedule>>(emptyList())
    val scheduledMedications: State<List<MedSchedule>> get() = _scheduledMedications

    fun updateMedicationName(name: String) {
        _medicationName.value = name
    }

    fun updateMedicationTime(time: String) {
        _medicationTime.value = time
    }

    fun scheduleNotification(time: LocalTime) {
        val currentTime = LocalTime.now()
        val delay = if (time.isAfter(currentTime)) {
            ChronoUnit.MILLIS.between(currentTime, time)
        } else {
            ChronoUnit.MILLIS.between(currentTime, time.plus(24, ChronoUnit.HOURS))
        }

        val workId = UUID.randomUUID().toString()

        val data = Data.Builder()
            .putString("medicationName", _medicationName.value)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag(workId)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)

        val newSchedule = MedSchedule(
            name = _medicationName.value,
            time = _medicationTime.value,
            workId = workId
        )

        _scheduledMedications.value = _scheduledMedications.value + newSchedule
    }

    fun deleteMedication(medSchedule: MedSchedule) {
        // Cancel the scheduled WorkManager notification
        WorkManager.getInstance(context).cancelWorkById(UUID.fromString(medSchedule.workId))

        // Remove the medication from the list
        _scheduledMedications.value = _scheduledMedications.value.filter { it.workId != medSchedule.workId }
    }
}