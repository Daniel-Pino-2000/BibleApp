package com.application.bibleapp.viewmodel

import com.application.bibleapp.data.repository.BibleRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.bibleapp.data.model.BibleBooks
import com.application.bibleapp.data.model.VerseUI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BibleViewModel(private val repository: BibleRepository): ViewModel() {

    private val _verses = MutableStateFlow<List<VerseUI>>(emptyList())
    val verses: StateFlow<List<VerseUI>> = _verses

    private val _currentBook = MutableStateFlow(1)
    val currentBook: StateFlow<Int> = _currentBook

    private val _currentChapter = MutableStateFlow(1)
    val currentChapter: StateFlow<Int> = _currentChapter

    init {
        // Load chapter 1 by default
        loadChapter(_currentBook.value, _currentChapter.value)
    }

    fun loadChapter(bookId: Int, chapterId: Int) {
        viewModelScope.launch {
            val chapterData = repository.getChapter(bookId, chapterId)
            _verses.value = chapterData
        }
    }

    fun previousChapter() {
        if (_currentChapter.value > 1) {
            _currentChapter.value -= 1
            loadChapter(_currentBook.value, _currentChapter.value)
        } else if (_currentBook.value > 1) {
            _currentBook.value -= 1
            val previousBook = BibleBooks.allBooks.first { it.id == _currentBook.value }
            _currentChapter.value = previousBook.chapters.size
        }
    }

    fun nextChapter() {
        val currentBook = BibleBooks.allBooks.first { it.id == _currentBook.value }

        if (_currentChapter.value < currentBook.chapters.size) {
            // Go to next chapter in the same book
            _currentChapter.value += 1
        } else if (_currentBook.value < BibleBooks.allBooks.size) {
            // Move to the first chapter of the next book
            _currentBook.value += 1
            _currentChapter.value = 1
        } else {
            // Already at the last chapter of the last book
            return
        }
        loadChapter(_currentBook.value, _currentChapter.value)
    }


}