package com.application.bibleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.bibleapp.data.model.BibleBooks
import com.application.bibleapp.data.model.BibleTranslation
import com.application.bibleapp.data.model.DEFAULT_VERSION
import com.application.bibleapp.data.model.SelectedBibleVersion
import com.application.bibleapp.data.model.VerseUI
import com.application.bibleapp.data.remote.LanguageGroup
import com.application.bibleapp.data.remote.groupVersionsByLanguage
import com.application.bibleapp.data.repository.BibleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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

    private val _availableVersions = MutableStateFlow<List<BibleTranslation>>(emptyList())
    val availableVersions: StateFlow<List<BibleTranslation>> = _availableVersions.asStateFlow()

    private val _isLoadingVersions = MutableStateFlow(false)
    val isLoadingVersions: StateFlow<Boolean> = _isLoadingVersions.asStateFlow()

    private val _versionsError = MutableStateFlow<String?>(null)
    val versionsError: StateFlow<String?> = _versionsError.asStateFlow()

    private val _versionSearchQuery = MutableStateFlow("")
    val versionSearchQuery: StateFlow<String> = _versionSearchQuery.asStateFlow()

    // Derived from availableVersions + the search query — recomputed locally, no re-fetch.
    val groupedVersions: StateFlow<List<LanguageGroup>> =
        combine(_availableVersions, _versionSearchQuery) { versions, query ->
            groupVersionsByLanguage(versions, query)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedVersion = MutableStateFlow(DEFAULT_VERSION)
    val selectedVersion: StateFlow<SelectedBibleVersion> = _selectedVersion.asStateFlow()

    // Download state
    private val _downloadingVersionId = MutableStateFlow<String?>(null)
    val downloadingVersionId: StateFlow<String?> = _downloadingVersionId.asStateFlow()

    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress.asStateFlow()

    private val _downloadError = MutableStateFlow<String?>(null)
    val downloadError: StateFlow<String?> = _downloadError.asStateFlow()

    // Informational (not an error): set when a download succeeded but some content
    // wasn't available for this version (e.g. an NT-only translation).
    private val _downloadInfo = MutableStateFlow<String?>(null)
    val downloadInfo: StateFlow<String?> = _downloadInfo.asStateFlow()

    init {
        loadChapter(_currentBook.value, _currentChapter.value)
        loadAvailableVersions()
    }

    fun loadChapter(bookId: Int, chapterId: Int, verseId: Int = 1) {
        viewModelScope.launch {
            val chapterData = repository.getChapter(
                bookId = bookId,
                chapter = chapterId,
                versionId = _selectedVersion.value.id
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
                _searchResults.value = repository.searchVerses(query, _selectedVersion.value.id)
            }
        }
    }

    fun clearSearchResults() {
        _searchResults.value = emptyList()
        _searchQuery.value = ""
    }

    fun onVersionSearchQueryChange(query: String) {
        _versionSearchQuery.value = query
    }

    fun loadAvailableVersions() {
        viewModelScope.launch {
            _isLoadingVersions.value = true
            _versionsError.value = null
            try {
                _availableVersions.value = repository.getAllVersions()
            } catch (e: Exception) {
                _versionsError.value = e.message
            } finally {
                _isLoadingVersions.value = false
            }
        }
    }

    /**
     * If already downloaded, switches immediately.
     * If not, downloads first then switches.
     * [onFinished] fires with true if the version is now selected (either it
     * was already downloaded, or the download just succeeded), false on failure —
     * callers can use this to decide whether it's safe to navigate away.
     *
     * Guards against re-entrancy: the UI already disables the row while a
     * download is in flight, but this check is synchronous (set before the
     * coroutine is even launched) so a double-tap that beats recomposition
     * can't start a second overlapping download of the same DB connection.
     */
    fun selectVersion(versionId: String, onFinished: (success: Boolean) -> Unit = {}) {
        if (_downloadingVersionId.value != null) return
        _downloadingVersionId.value = versionId
        _downloadInfo.value = null

        viewModelScope.launch {
            if (repository.isVersionDownloaded(versionId)) {
                _downloadingVersionId.value = null
                switchToVersion(versionId)
                onFinished(true)
                return@launch
            }
            val success = downloadAndSwitch(versionId)
            onFinished(success)
        }
    }

    fun useLocalBible() {
        _selectedVersion.value = DEFAULT_VERSION
        loadChapter(_currentBook.value, _currentChapter.value)
    }

    private fun switchToVersion(versionId: String) {
        _selectedVersion.value = SelectedBibleVersion(id = versionId)
        loadChapter(_currentBook.value, _currentChapter.value)
    }

    private suspend fun downloadAndSwitch(versionId: String): Boolean {
        _downloadingVersionId.value = versionId
        _downloadProgress.value = 0f
        _downloadError.value = null

        val result = repository.downloadVersion(
            translationId = versionId,
            onProgress = { _downloadProgress.value = it }
        )

        _downloadingVersionId.value = null

        return result.fold(
            onSuccess = { summary ->
                if (summary.skippedBookCount > 0) {
                    _downloadInfo.value = "Downloaded ${summary.downloadedVerseCount} verses across " +
                        "${summary.downloadedChapterCount} chapters — some books aren't available in this translation."
                }
                switchToVersion(versionId)
                true
            },
            onFailure = {
                _downloadError.value = it.message ?: "Download failed"
                false
            }
        )
    }
}