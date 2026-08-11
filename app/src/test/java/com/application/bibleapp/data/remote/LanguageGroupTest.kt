package com.application.bibleapp.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LanguageGroupTest {

    private fun version(id: String, version: String, language: String, langCode: String, description: String? = null) =
        BibleVersionDto(
            id = id,
            version = version,
            description = description,
            language = LanguageDto(name = language, code = langCode)
        )

    private val sample = listOf(
        version("en-kjv", "KJV", "English", "en"),
        version("en-asv", "ASV", "English", "en"),
        version("grc-grcbrent", "Brenton", "Ancient Greek", "grc"),
        version("es-rvr", "RVR", "Spanish", "es", description = "Reina Valera")
    )

    @Test
    fun `groups are sorted alphabetically by language name`() {
        val groups = groupVersionsByLanguage(sample)

        assertEquals(listOf("Ancient Greek", "English", "Spanish"), groups.map { it.languageName })
    }

    @Test
    fun `versions within a group are sorted by version name`() {
        val groups = groupVersionsByLanguage(sample)
        val english = groups.first { it.languageName == "English" }

        assertEquals(listOf("ASV", "KJV"), english.versions.map { it.version })
    }

    @Test
    fun `every version ends up in exactly one group`() {
        val groups = groupVersionsByLanguage(sample)

        assertEquals(sample.size, groups.sumOf { it.versions.size })
    }

    @Test
    fun `search matches language name case-insensitively`() {
        val groups = groupVersionsByLanguage(sample, query = "greek")

        assertEquals(listOf("Ancient Greek"), groups.map { it.languageName })
    }

    @Test
    fun `search matches version name and description`() {
        val byVersion = groupVersionsByLanguage(sample, query = "kjv")
        val byDescription = groupVersionsByLanguage(sample, query = "valera")

        assertEquals(listOf("KJV"), byVersion.flatMap { it.versions }.map { it.version })
        assertEquals(listOf("RVR"), byDescription.flatMap { it.versions }.map { it.version })
    }

    @Test
    fun `search with no matches returns no groups`() {
        val groups = groupVersionsByLanguage(sample, query = "klingon")

        assertTrue(groups.isEmpty())
    }

    @Test
    fun `blank query returns everything ungrouped by filter`() {
        val groups = groupVersionsByLanguage(sample, query = "   ")

        assertEquals(sample.size, groups.sumOf { it.versions.size })
    }
}
