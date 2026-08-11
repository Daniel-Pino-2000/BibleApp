package com.application.bibleapp.data.remote

import com.application.bibleapp.data.model.TextDirection
import org.junit.Assert.assertEquals
import org.junit.Test

class HelloAoDtosTest {

    @Test
    fun `prefers shortName as the display name when present`() {
        val dto = HelloAoTranslationDto(
            id = "BSB", name = "Berean Standard Bible", englishName = "Berean Standard Bible",
            shortName = "BSB", language = "eng", languageEnglishName = "English"
        )

        val translation = dto.toBibleTranslation()

        assertEquals("BSB", translation.displayName)
        assertEquals("Berean Standard Bible", translation.nativeName)
        assertEquals("English", translation.languageName)
        assertEquals(TextDirection.LTR, translation.textDirection)
    }

    @Test
    fun `falls back to englishName when shortName is missing`() {
        val dto = HelloAoTranslationDto(
            id = "GHT", name = "Garth's Hyper-literal Translation", englishName = "Garth's Hyper-literal Translation",
            shortName = null, language = "eng"
        )

        assertEquals("Garth's Hyper-literal Translation", dto.toBibleTranslation().displayName)
    }

    @Test
    fun `rtl text direction is preserved for right-to-left languages`() {
        val dto = HelloAoTranslationDto(
            id = "ARBNAV", name = "كتاب الحياة", englishName = "New Arabic Version (Book of Life)",
            shortName = "NAV", language = "arb", textDirection = "rtl",
            languageName = "العربية", languageEnglishName = "Arabic"
        )

        val translation = dto.toBibleTranslation()

        assertEquals(TextDirection.RTL, translation.textDirection)
        assertEquals("Arabic", translation.languageName)
        assertEquals("كتاب الحياة", translation.nativeName)
    }

    @Test
    fun `falls back to native languageName when languageEnglishName is absent`() {
        val dto = HelloAoTranslationDto(
            id = "X", name = "X", englishName = "X", language = "xx", languageName = "Xish"
        )

        assertEquals("Xish", dto.toBibleTranslation().languageName)
    }
}
