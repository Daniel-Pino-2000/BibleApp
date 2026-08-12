package com.application.bibleapp.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Dedicated text styles for the reading surface (verse body, headings, footnotes) —
 * kept separate from the UI [Typography] scale because reading text needs its own
 * family (serif, vs. the sans used for chrome) and looser line-height than any M3
 * slot is meant to carry.
 */
object ReadingStyle {
    /** Verse body text. ~1.6x line-height for long-form reading comfort. */
    val VerseText = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 29.sp,
        letterSpacing = 0.1.sp
    )

    /** Verse number marker — deliberately small/light so it stays subordinate to the text. */
    val VerseNumber = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )

    /** Section heading preceding a passage (e.g. "The Sermon on the Mount"). */
    val SectionHeading = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp,
        lineHeight = 26.sp
    )

    /** Hebrew subtitle / psalm superscription. */
    val HebrewSubtitle = TextStyle(
        fontFamily = FontFamily.Serif,
        fontStyle = FontStyle.Italic,
        fontSize = 15.sp,
        lineHeight = 22.sp
    )

    /** Footnote bottom sheet body. */
    val FootnoteBody = TextStyle(
        fontFamily = FontFamily.Serif,
        fontSize = 15.sp,
        lineHeight = 23.sp
    )
}

/** Adjustable verse text size, persisted via [com.application.bibleapp.data.repository.BibleRepository]. */
enum class VerseTextScale(val multiplier: Float, val label: String) {
    SMALL(0.85f, "Small"),
    DEFAULT(1f, "Default"),
    LARGE(1.15f, "Large"),
    EXTRA_LARGE(1.3f, "Extra Large");

    companion object {
        fun fromMultiplier(value: Float): VerseTextScale =
            entries.minByOrNull { kotlin.math.abs(it.multiplier - value) } ?: DEFAULT
    }
}

/** Scales font size and line height together, preserving other style attributes. */
fun TextStyle.scaledBy(factor: Float): TextStyle = copy(
    fontSize = fontSize * factor,
    lineHeight = if (lineHeight != TextUnit.Unspecified) lineHeight * factor else lineHeight
)
