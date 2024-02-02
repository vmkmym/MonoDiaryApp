package com.example.monodiaryapp.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.monodiaryapp.data.DiaryDatabase
import com.example.monodiaryapp.data.DiaryEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

class DiaryViewModel : ViewModel() {
    private val _titleState = MutableStateFlow("")
    val titleState: StateFlow<String> = _titleState

    private val _mainTextState = MutableStateFlow("")
    val mainTextState: StateFlow<String> = _mainTextState

    private val _bgmState = MutableStateFlow("")
    val bgmState: StateFlow<String> = _bgmState

    private val _imageUris = MutableStateFlow<List<Uri>>(emptyList())
    val imageUris: StateFlow<List<Uri>> = _imageUris

    private val _dateState = MutableStateFlow(LocalDate.now())
    val dateState: StateFlow<LocalDate> = _dateState

    private val _selectedDiary = MutableStateFlow<DiaryEntry?>(null)
    val selectedDiary: StateFlow<DiaryEntry?> = _selectedDiary

    private val _uidState = MutableStateFlow(0L)
    val uidState: StateFlow<Long> = _uidState


    fun initialize(
        initialTitle: String, initialMainText: String, initialBgm: String, initialUid
        : Long
    ) {
        _titleState.value = initialTitle
        _mainTextState.value = initialMainText
        _bgmState.value = initialBgm
        _uidState.value = initialUid
    }
    fun updateTitle(newTitle: String) {
        _titleState.value = newTitle
    }

    fun updateMainText(newMainText: String) {
        _mainTextState.value = newMainText
    }

    fun updateBgm(newBgm: String) {
        _bgmState.value = newBgm
    }

    fun updateImageUris(newImageUris: List<Uri>) {
        _imageUris.value = newImageUris
    }
}

class DiaryViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiaryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DiaryViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}