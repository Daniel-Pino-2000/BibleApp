package com.application.bibleapp.data.model

data class SelectedBibleVersion(
    val id: String?,              // Example: "kjv" or "es-rvr1960"
    val source: BibleSourceType  // LOCAL or REMOTE
)
