package com.application.bibleapp.viewmodel

import com.application.bibleapp.data.repository.BibleRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.bibleapp.data.model.VerseUI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BibleViewModel(private val repository: BibleRepository): ViewModel() {

    private val _verses = MutableStateFlow<List<VerseUI>>(emptyList())
    val verses: StateFlow<List<VerseUI>> = _verses

    fun loadChapter(bookId: Int, chapterId: Int) {
        viewModelScope.launch {
            val chapterData = repository.getChapter(bookId, chapterId)
            _verses.value = chapterData
        }
    }


}