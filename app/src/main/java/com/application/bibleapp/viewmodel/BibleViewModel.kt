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


    fun loadChapter(bookId: Int, chapterId: Int, verseId: Int = 1) {
        viewModelScope.launch {
            val chapterData = repository.getChapter(bookId, chapterId)
            _verses.value = chapterData
            _currentBook.value = bookId
            _currentChapter.value = chapterId
            _currentVerse.value = verseId.coerceIn(1, chapterData.size) // safe bounds
        }
    }

    fun previousChapter() {
        if (_currentChapter.value > 1) {
            loadChapter(_currentBook.value, _currentChapter.value - 1) // defaults to verse 1
        } else if (_currentBook.value > 1) {
            val previousBook = BibleBooks.allBooks.first { it.id == _currentBook.value - 1 }
            loadChapter(previousBook.id, previousBook.chapters.size) // defaults to verse 1
        }
    }

    fun nextChapter() {
        val currentBook = BibleBooks.allBooks.first { it.id == _currentBook.value }
        if (_currentChapter.value < currentBook.chapters.size) {
            loadChapter(_currentBook.value, _currentChapter.value + 1) // defaults to verse 1
        } else if (_currentBook.value < BibleBooks.allBooks.size) {
            loadChapter(_currentBook.value + 1, 1) // defaults to verse 1
        }
    }

    fun setBook(bookId: Int, chapter: Int = 1, verse: Int = 1) {
        _currentBook.value = bookId
        _currentChapter.value = chapter
        loadChapter(bookId, chapter, verse)
    }

    fun setChapter(chapterId: Int) {
        _currentChapter.value = chapterId
        loadChapter(_currentBook.value, chapterId)
    }

    fun setVerse(verseNumber: Int) {
        _currentVerse.value = verseNumber
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

    fun clearSearchResults() {
        _searchResults.value = emptyList()
        _searchQuery.value = ""  // optional, if you want to clear the search query too
    }





}