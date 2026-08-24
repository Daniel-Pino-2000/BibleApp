package com.application.bibleapp.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class OurMannaResponseDto(
    val verse: OurMannaVerseDto
)

@Serializable
data class OurMannaVerseDto(
    val details: OurMannaVerseDetailsDto,
    val notice: String
)

@Serializable
data class OurMannaVerseDetailsDto(
    val text: String,
    val reference: String,
    val version: String,
    val verseurl: String
)

