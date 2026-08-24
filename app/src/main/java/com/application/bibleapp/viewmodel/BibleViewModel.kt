package com.application.bibleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.bibleapp.data.model.BibleBooks
import com.application.bibleapp.data.model.BibleTranslation
import com.application.bibleapp.data.model.DEFAULT_VERSION
import com.application.bibleapp.data.model.DailyVerseRef
import com.application.bibleapp.data.model.DailyVerseUI
import com.application.bibleapp.data.model.DownloadedVersionInfo
import com.application.bibleapp.data.model.Footnote
import com.application.bibleapp.data.model.SelectedBibleVersion
import com.application.bibleapp.data.model.VerseOfTheDay
import com.application.bibleapp.data.model.VerseUI
import com.application.bibleapp.data.model.resolveDailyVerseUI
import com.application.bibleapp.data.remote.LanguageGroup
import com.application.bibleapp.data.remote.groupVersionsByLanguage
import com.application.bibleapp.data.repository.BibleRepository
import com.application.bibleapp.ui.theme.ThemeMode
import com.application.bibleapp.ui.theme.VerseTextScale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Version state is split across three flows that the version picker composes
 * together rather than one combined "picker state" object:
 * - [selectedVersion] — which translation is on screen right now.
 * - [downloadedVersions] — every translation that actually has rows in local
 *   storage, keyed by id with a [DownloadedVersionInfo.schemaVersion] so the
 *   picker can tell "downloaded" apart from "downloaded but stale" (see
 *   [refreshDownloadedVersions]). This is queried from SQLite once per
 *   version-list load, not per row, and re-queried after every
 *   download/re-download so it never drifts from what's on disk.
 * - [downloadingVersionId]/[downloadProgress] — in-flight download state,
 *   `null` when nothing is downloading.
 *
 * Kept separate because they change independently: switching versions doesn't
 * touch the downloaded set, and a download in progress doesn't change which
 * version is currently selected until it finishes.
 */
class BibleViewModel(private val repository: BibleRepository) : ViewModel() {

    private val _verses = MutableStateFlow<List<VerseUI>>(emptyList())
    val verses: StateFlow<List<VerseUI>> = _verses.asStateFlow()

    private val _footnotes = MutableStateFlow<List<Footnote>>(emptyList())
    val footnotes: StateFlow<List<Footnote>> = _footnotes.asStateFlow()

    // The footnote currently shown in a bottom sheet, or null when none is open.
    private val _selectedFootnote = MutableStateFlow<Footnote?>(null)
    val selectedFootnote: StateFlow<Footnote?> = _selectedFootnote.asStateFlow()

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

    /**
     * Short label for the version button (e.g. "KJV", "ESV") — looked up from the
     * translation catalog by id. Falls back to "KJV" for the bundled default (it has no
     * catalog entry, it isn't a real helloao translation id) and to the raw id, uppercased,
     * if the catalog hasn't loaded yet.
     */
    val currentVersionLabel: StateFlow<String> =
        combine(_selectedVersion, _availableVersions) { selected, versions ->
            versions.firstOrNull { it.id == selected.id }?.displayName
                ?: if (selected.id == DEFAULT_VERSION.id) "KJV" else selected.id.uppercase()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "KJV")

    private val _themeMode = MutableStateFlow(repository.loadThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _verseTextScale = MutableStateFlow(repository.loadVerseTextScale())
    val verseTextScale: StateFlow<VerseTextScale> = _verseTextScale.asStateFlow()

    // Shown on Home — fetched independently of currentBook/currentChapter so it
    // doesn't disturb the user's actual reading position.
    private val _verseOfTheDay = MutableStateFlow<DailyVerseUI?>(null)
    val verseOfTheDay: StateFlow<DailyVerseUI?> = _verseOfTheDay.asStateFlow()

    // Locally downloaded versions, keyed by id, so the picker can badge "downloaded" /
    // "update available" without a DB query per row. Refreshed after every download.
    private val _downloadedVersions = MutableStateFlow<Map<String, DownloadedVersionInfo>>(emptyMap())
    val downloadedVersions: StateFlow<Map<String, DownloadedVersionInfo>> = _downloadedVersions.asStateFlow()

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
        // The last version the user picked is persisted across process restarts. Only
        // trust it if it's still actually downloaded — local data may have been cleared.
        viewModelScope.launch {
            val persistedId = repository.loadSelectedVersion()
            val isAvailable = persistedId == DEFAULT_VERSION.id || repository.isVersionDownloaded(persistedId)
            _selectedVersion.value = if (isAvailable) SelectedBibleVersion(id = persistedId) else DEFAULT_VERSION
            val lastPosition = repository.loadReadingPosition()
            loadChapter(lastPosition.bookId, lastPosition.chapter, lastPosition.verse)
            loadVerseOfTheDay()
        }
        loadAvailableVersions()
        refreshDownloadedVersions()
    }

    // Tries the remote daily-verse API first (cached once per day by the repository);
    // falls back to the local curated list if the app is offline, the API is down, or
    // it returns a reference this app can't resolve (unrecognized book name, verse not
    // present in the currently selected translation, etc.) — this must never throw, so
    // the Home screen always has something to show instead of crashing on launch.
    private fun loadVerseOfTheDay() {
        viewModelScope.launch {
            val remoteVerse = runCatching { repository.getDailyVerse() }
                .mapCatching { ref -> resolveVerse(ref) ?: error("Reference not found: $ref") }
                .getOrNull()
            _verseOfTheDay.value = remoteVerse ?: resolveVerse(VerseOfTheDay.forToday())
        }
    }

    private suspend fun resolveVerse(ref: DailyVerseRef): DailyVerseUI? {
        val bookName = BibleBooks.getBookById(ref.bookId)?.name ?: return null
        val chapterVerses = repository.getChapter(ref.bookId, ref.chapter, _selectedVersion.value.id)
        return resolveDailyVerseUI(bookName, ref, chapterVerses)
    }

    fun loadChapter(bookId: Int, chapterId: Int, verseId: Int = 1) {
        viewModelScope.launch {
            val versionId = _selectedVersion.value.id
            val chapterData = repository.getChapter(bookId = bookId, chapter = chapterId, versionId = versionId)
            _verses.value = chapterData
            _footnotes.value = repository.getFootnotes(bookId = bookId, chapter = chapterId, versionId = versionId)
            _currentBook.value = bookId
            _currentChapter.value = chapterId
            val verse = verseId.coerceIn(1, chapterData.size.coerceAtLeast(1))
            _currentVerse.value = verse
            repository.saveReadingPosition(bookId, chapterId, verse)
        }
    }

    /** Opens the footnote bottom sheet for [noteId] in the chapter currently on screen, if found. */
    fun selectFootnote(noteId: Int) {
        _selectedFootnote.value = _footnotes.value.firstOrNull { it.noteId == noteId }
    }

    fun dismissFootnote() {
        _selectedFootnote.value = null
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

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        repository.saveThemeMode(mode)
    }

    fun setVerseTextScale(scale: VerseTextScale) {
        _verseTextScale.value = scale
        repository.saveVerseTextScale(scale)
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
     * If already the active version, this is a no-op (no re-fetch, no re-switch) —
     * the caller still gets [onFinished](true), since from the UI's point of view
     * nothing needs to happen but the picker can still be dismissed.
     * If already downloaded, switches immediately.
     * If not, downloads first then switches.
     * [onFinished] fires with true if the version is now selected (either it
     * was already active/downloaded, or the download just succeeded), false on
     * failure — callers can use this to decide whether it's safe to navigate away.
     *
     * Guards against re-entrancy: the UI already disables the row while a
     * download is in flight, but this check is synchronous (set before the
     * coroutine is even launched) so a double-tap that beats recomposition
     * can't start a second overlapping download of the same DB connection.
     */
    fun selectVersion(versionId: String, onFinished: (success: Boolean) -> Unit = {}) {
        if (versionId == _selectedVersion.value.id) {
            onFinished(true)
            return
        }
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

    /**
     * Re-downloads an already-downloaded version from scratch — the only way to pick
     * up a newer [DownloadedVersionInfo.schemaVersion] (e.g. a version downloaded
     * before footnotes existed). No-ops while any other download is in flight.
     */
    fun redownloadVersion(versionId: String) {
        if (_downloadingVersionId.value != null) return
        _downloadingVersionId.value = versionId
        _downloadProgress.value = 0f
        _downloadError.value = null
        _downloadInfo.value = null

        viewModelScope.launch {
            val result = repository.redownloadVersion(
                translationId = versionId,
                onProgress = { _downloadProgress.value = it }
            )
            _downloadingVersionId.value = null

            result.fold(
                onSuccess = {
                    refreshDownloadedVersions()
                    // Reload so an already-open chapter picks up the newly-fetched footnotes/rich content.
                    if (_selectedVersion.value.id == versionId) {
                        loadChapter(_currentBook.value, _currentChapter.value)
                    }
                },
                onFailure = { _downloadError.value = it.message ?: "Download failed" }
            )
        }
    }

    fun useLocalBible() {
        switchToVersion(DEFAULT_VERSION.id)
    }

    private fun switchToVersion(versionId: String) {
        _selectedVersion.value = SelectedBibleVersion(id = versionId)
        repository.saveSelectedVersion(versionId)
        loadChapter(_currentBook.value, _currentChapter.value)
    }

    private fun refreshDownloadedVersions() {
        viewModelScope.launch {
            _downloadedVersions.value = repository.getDownloadedVersions().associateBy { it.id }
        }
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
                refreshDownloadedVersions()
                true
            },
            onFailure = {
                _downloadError.value = it.message ?: "Download failed"
                false
            }
        )
    }
}