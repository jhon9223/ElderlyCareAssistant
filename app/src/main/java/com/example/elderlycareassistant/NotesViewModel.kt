package com.example.elderlycareassistant

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class NotesViewModel(private val dao: NoteDao) : ViewModel() {
    private val _noteEntities = mutableStateOf<List<NoteEntity>>(emptyList())
    val noteEntities: State<List<NoteEntity>> get() = _noteEntities

    val notes: List<String> get() = _noteEntities.value.map { it.noteText }

    init {
        viewModelScope.launch {
            dao.getAllNotes().collect { entities ->
                _noteEntities.value = entities
            }
        }
    }

    fun addNote(noteText: String, date: String, time: String) {
        if (noteText.isNotBlank() && date.isNotBlank() && time.isNotBlank()) {
            viewModelScope.launch {
                dao.insert(NoteEntity(noteText = noteText, date = date, time = time))
            }
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            dao.delete(note)
        }
    }
}