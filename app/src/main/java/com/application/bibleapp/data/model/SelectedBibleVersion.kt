package com.application.bibleapp.data.model

data class SelectedBibleVersion(
    val id: String  // "kjv" for bundled, or any downloaded version id
)

val DEFAULT_VERSION = SelectedBibleVersion(id = "kjv")