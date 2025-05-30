package com.example.elderlycareassistant

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            // Permission denied; you might want to inform the user
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            val navController = rememberNavController()
            val medicationViewModel: MedicationViewModel = viewModel(
                factory = MedicationViewModelFactory(applicationContext)
            )
            NavHost(navController = navController, startDestination = "medication") {
                composable("medication") {
                    MedicationScreen(medicationViewModel, navController)
                }
                composable("notes") {
                    val notesViewModel: NotesViewModel = viewModel(
                        factory = NotesViewModelFactory(applicationContext)
                    )
                    NotesScreen(notesViewModel)
                }
                composable("emergency") {
                    EmergencyScreen()
                }
                composable("patientInfo") {
                    val patientInfoViewModel: PatientInfoViewModel = viewModel(
                        factory = PatientInfoViewModelFactory(applicationContext)
                    )
                    PatientInfoScreen(patientInfoViewModel)
                }
            }
        }
    }
}

@Composable
fun MedicationScreen(viewModel: MedicationViewModel, navController: NavHostController) {
    var medicationName by remember { mutableStateOf("") }
    var medicationTime by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val scheduledMedications by viewModel.scheduledMedications

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.medicine),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(100.dp)
                .padding(bottom = 16.dp)
        )

        TextField(
            value = medicationName,
            onValueChange = { medicationName = it },
            label = { Text("Medication Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = medicationTime,
            onValueChange = {
                medicationTime = it
                errorMessage = ""
            },
            label = { Text("Time (HH:mm)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        Button(
            onClick = {
                if (medicationTime.isEmpty()) {
                    errorMessage = "Please enter a time"
                } else {
                    try {
                        val time = LocalTime.parse(medicationTime, DateTimeFormatter.ofPattern("HH:mm"))
                        viewModel.updateMedicationName(medicationName)
                        viewModel.updateMedicationTime(medicationTime)
                        viewModel.scheduleNotification(time)
                        medicationName = ""
                        medicationTime = ""
                        errorMessage = ""
                    } catch (e: Exception) {
                        errorMessage = "Invalid time format. Use HH:mm (e.g., 14:30)"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = medicationTime.isNotEmpty()
        ) {
            Text("Schedule Notification")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (scheduledMedications.isNotEmpty()) {
            Text(
                text = "Scheduled Medications",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(scheduledMedications) { medSchedule ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = medSchedule.name,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = medSchedule.time,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = { viewModel.deleteMedication(medSchedule) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                            ) {
                                Text("Delete", color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate("notes") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Go to Appointment Notes")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { navController.navigate("emergency") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text("Emergency Numbers", color = Color.White)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { navController.navigate("patientInfo") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Patient Info")
        }
    }
}

@Composable
fun NotesScreen(viewModel: NotesViewModel) {
    var noteText by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val noteEntities by viewModel.noteEntities

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Appointment Notes",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TextField(
            value = noteText,
            onValueChange = { noteText = it },
            label = { Text("Enter Appointment Note") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Date (yyyy-MM-dd)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = time,
            onValueChange = { time = it },
            label = { Text("Time (HH:mm)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Button(
            onClick = {
                if (date.isEmpty() || time.isEmpty()) {
                    errorMessage = "Please enter both date and time"
                } else {
                    try {
                        // Validate date format (yyyy-MM-dd)
                        val datePattern = "\\d{4}-\\d{2}-\\d{2}".toRegex()
                        if (!date.matches(datePattern)) {
                            throw IllegalArgumentException("Invalid date format")
                        }
                        // Validate time format (HH:mm)
                        LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
                        viewModel.addNote(noteText, date, time)
                        noteText = ""
                        date = ""
                        time = ""
                        errorMessage = ""
                    } catch (e: Exception) {
                        errorMessage = "Invalid format. Use yyyy-MM-dd for date and HH:mm for time"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = noteText.isNotBlank() && date.isNotBlank() && time.isNotBlank()
        ) {
            Text("Save Note")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (noteEntities.isNotEmpty()) {
            Text(
                text = "Saved Notes",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(noteEntities) { note ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = note.noteText,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = { viewModel.deleteNote(note) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                                ) {
                                    Text("Delete", color = Color.White)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Date: ${note.date}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Time: ${note.time}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmergencyScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Emergency Numbers",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Red),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Ambulance: 911",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Police: 100",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Care taker: 7544456847",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Lakeshore Hospital: 8283456257",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Fire Service: 101",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun PatientInfoScreen(viewModel: PatientInfoViewModel) {
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    val patientInfoList by viewModel.patientInfoList

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Patient Information",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Weight (kg)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = height,
            onValueChange = { height = it },
            label = { Text("Height (cm)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                viewModel.addPatientInfo(weight, height)
                weight = ""
                height = ""
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = weight.isNotBlank() && height.isNotBlank()
        ) {
            Text("Save Patient Info")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (patientInfoList.isNotEmpty()) {
            Text(
                text = "Saved Patient Info",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(patientInfoList) { info ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Weight: ${info.weight} kg",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "Height: ${info.height} cm",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = { viewModel.deletePatientInfo(info) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                            ) {
                                Text("Delete", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}