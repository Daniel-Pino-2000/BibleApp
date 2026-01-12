package com.application.bibleapp.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class BibleVersionDto(
    val id: String,
    val version: String,
    val description: String? = null,
    val scope: String? = null,
    val language: LanguageDto,
    val country: CountryDto? = null
)

@Serializable
data class LanguageDto(
    val name: String,
    val code: String,
    val level: String? = null
)

@Serializable
data class CountryDto(
    val name: String,
    val code: String
)

