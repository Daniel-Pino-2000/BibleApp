package com.application.bibleapp.viewmodel

import com.application.bibleapp.data.repository.BibleRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.bibleapp.data.model.BibleBooks
import com.application.bibleapp.data.model.BibleSourceType
import com.application.bibleapp.data.model.SelectedBibleVersion
import com.application.bibleapp.data.model.VerseUI
import com.application.bibleapp.data.remote.BibleVersionDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BibleViewModel(private val repository: BibleRepository) : ViewModel() {

    private val _verses = MutableStateFlow<List<VerseUI>>(emptyList())
    val verses: StateFlow<List<VerseUI>> = _verses.asStateFlow()

    private val _currentBook = MutableStateFlow(1)
    val currentBook: StateFlow<Int> = _currentBook

    private val _currentChapter = MutableStateFlow(1)
    val currentChapter: StateFlow<Int> = _currentChapter

    private val _currentVerse = MutableStateFlow(1)
    val currentVerse: StateFlow<Int> = _currentVerse

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<VerseUI>>(emptyList())
    val searchResults: StateFlow<List<VerseUI>> = _searchResults.asStateFlow()

    private val _availableVersions = MutableStateFlow<List<BibleVersionDto>>(emptyList())
    val availableVersions: StateFlow<List<BibleVersionDto>> = _availableVersions.asStateFlow()

    private val _isLoadingVersions = MutableStateFlow(false)
    val isLoadingVersions: StateFlow<Boolean> = _isLoadingVersions.asStateFlow()

    private val _versionsError = MutableStateFlow<String?>(null)
    val versionsError: StateFlow<String?> = _versionsError.asStateFlow()

    private val _selectedVersion = MutableStateFlow<SelectedBibleVersion>(
        SelectedBibleVersion(null, BibleSourceType.LOCAL)
    )
    val selectedVersion: StateFlow<SelectedBibleVersion> = _selectedVersion.asStateFlow()

    init {
        loadChapter(_currentBook.value, _currentChapter.value)
        loadAvailableVersions()
    }

    fun loadChapter(bookId: Int, chapterId: Int, verseId: Int = 1) {
        viewModelScope.launch {
            // ✅ FIX: pass the current selected version directly at call time,
            // so the repository always gets the live value, not a stale snapshot
            val chapterData = repository.getChapter(
                bookId = bookId,
                chapter = chapterId,
                selectedVersion = _selectedVersion.value  // ✅ always fresh
            )
            _verses.value = chapterData
            _currentBook.value = bookId
            _currentChapter.value = chapterId
            _currentVerse.value = verseId.coerceIn(1, chapterData.size)
        }
    }

    fun previousChapter() {
        if (_currentChapter.value > 1) {
            loadChapter(_currentBook.value, _currentChapter.value - 1)
        } else if (_currentBook.value > 1) {
            val previousBook = BibleBooks.allBooks.first { it.id == _currentBook.value - 1 }
            loadChapter(previousBook.id, previousBook.chapters.size)
        }
    }

    fun nextChapter() {
        val currentBook = BibleBooks.allBooks.first { it.id == _currentBook.value }
        if (_currentChapter.value < currentBook.chapters.size) {
            loadChapter(_currentBook.value, _currentChapter.value + 1)
        } else if (_currentBook.value < BibleBooks.allBooks.size) {
            loadChapter(_currentBook.value + 1, 1)
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
        _searchQuery.value = ""
    }

    fun loadAvailableVersions() {
        viewModelScope.launch {
            _isLoadingVersions.value = true
            _versionsError.value = null
            try {
                println("Loading versions from API...")
                val versions = repository.getAllVersions()
                println("Fetched ${versions.size} versions")
                _availableVersions.value = versions
                if (versions.isEmpty()) {
                    println("WARNING: versions list is empty — check network or API response shape")
                }
            } catch (e: Exception) {
                println("Failed to load versions: $e")
                _versionsError.value = e.message
            } finally {
                _isLoadingVersions.value = false
            }
        }
    }

    fun setSelectedVersion(versionId: String) {
        _selectedVersion.value = SelectedBibleVersion(versionId, BibleSourceType.REMOTE)
        loadChapter(currentBook.value, currentChapter.value)  // reloads with new version
    }

    fun useLocalBible() {
        _selectedVersion.value = SelectedBibleVersion(null, BibleSourceType.LOCAL)
        loadChapter(currentBook.value, currentChapter.value)  // reloads with local
    }
}