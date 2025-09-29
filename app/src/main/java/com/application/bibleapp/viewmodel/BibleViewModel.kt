package com.application.bibleapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.application.bibleapp.data.repository.BibleRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.bibleapp.data.model.BibleBooks
import com.application.bibleapp.data.model.VerseUI
import com.application.bibleapp.utils.TextUtils.normalizeForSearch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BibleViewModel(private val repository: BibleRepository): ViewModel() {

    private val _verses = MutableStateFlow<List<VerseUI>>(emptyList())
    val verses: StateFlow<List<VerseUI>> = _verses.asStateFlow()

    private val _currentBook = MutableStateFlow(1)
    val currentBook: StateFlow<Int> = _currentBook

    private val _currentChapter = MutableStateFlow(1)
    val currentChapter: StateFlow<Int> = _currentChapter

    private val _currentVerse = MutableStateFlow(1)
    val currentVerse: StateFlow<Int> = _currentVerse

    init {
        // Load chapter 1 by default
        loadChapter(_currentBook.value, _currentChapter.value)
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<VerseUI>>(emptyList())
    val searchResults: StateFlow<List<VerseUI>> = _searchResults.asStateFlow()


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

    fun setBook(bookId: Int, chapter: Int = 1) {
        _currentBook.value = bookId
        _currentChapter.value = chapter
        loadChapter(bookId, chapter)
    }

    fun setChapter(chapterId: Int) {
        _currentChapter.value = chapterId
        loadChapter(_currentBook.value, chapterId)
    }

    fun setVerse(chapterId: Int) {
        _currentVerse.value = chapterId
        loadChapter(_currentVerse.value, chapterId)
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onSearchButtonClick() {
        val query = _searchQuery.value

        if (query.isNotBlank()) {
            viewModelScope.launch {
                _searchResults.value = repository.searchVerses(query)
            }
        }
    }




}