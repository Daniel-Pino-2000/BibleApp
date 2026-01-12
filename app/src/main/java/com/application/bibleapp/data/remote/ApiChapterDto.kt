package com.application.bibleapp.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class ApiChapterDto(
    val data: List<ApiVerseDto>
)
