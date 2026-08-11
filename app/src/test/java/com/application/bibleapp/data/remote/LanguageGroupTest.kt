package com.application.bibleapp.data.remote

import com.application.bibleapp.data.model.BibleTranslation
import com.application.bibleapp.data.model.TextDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LanguageGroupTest {

    private fun translation(
        id: String,
        displayName: String,
        languageName: String,
        langCode: String,
        nativeName: String = displayName
    ) = BibleTranslation(
        id = id,
        displayName = displayName,
        nativeName = nativeName,
        languageName = languageName,
        languageCode = langCode,
        textDirection = TextDirection.LTR
    )

    private val sample = listOf(
        translation("KJV", "KJV", "English", "eng"),
        translation("BSB", "BSB", "English", "eng", nativeName = "Berean Standard Bible"),
        translation("GRCBRENT", "Brenton", "Ancient Greek", "grc"),
        translation("ARBNAV", "NAV", "Arabic", "arb", nativeName = "كتاب الحياة")
    )

    @Test
    fun `groups are sorted alphabetically by language name`() {
        val groups = groupVersionsByLanguage(sample)

        assertEquals(listOf("Ancient Greek", "Arabic", "English"), groups.map { it.languageName })
    }

    @Test
    fun `translations within a group are sorted by display name`() {
        val groups = groupVersionsByLanguage(sample)
        val english = groups.first { it.languageName == "English" }

        assertEquals(listOf("BSB", "KJV"), english.translations.map { it.displayName })
    }

    @Test
    fun `every translation ends up in exactly one group`() {
        val groups = groupVersionsByLanguage(sample)

        assertEquals(sample.size, groups.sumOf { it.translations.size })
    }

    @Test
    fun `search matches language name case-insensitively`() {
        val groups = groupVersionsByLanguage(sample, query = "greek")

        assertEquals(listOf("Ancient Greek"), groups.map { it.languageName })
    }

    @Test
    fun `search matches display name and native name`() {
        val byDisplayName = groupVersionsByLanguage(sample, query = "kjv")
        val byNativeName = groupVersionsByLanguage(sample, query = "berean")

        assertEquals(listOf("KJV"), byDisplayName.flatMap { it.translations }.map { it.displayName })
        assertEquals(listOf("BSB"), byNativeName.flatMap { it.translations }.map { it.displayName })
    }

    @Test
    fun `search with no matches returns no groups`() {
        val groups = groupVersionsByLanguage(sample, query = "klingon")

        assertTrue(groups.isEmpty())
    }

    @Test
    fun `blank query returns everything ungrouped by filter`() {
        val groups = groupVersionsByLanguage(sample, query = "   ")

        assertEquals(sample.size, groups.sumOf { it.translations.size })
    }
}
