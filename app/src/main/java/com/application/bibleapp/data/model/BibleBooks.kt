package com.application.bibleapp.data.model

data class BibleBook(
    val id: Int,          // Matches bookNum in your DB
    val name: String,
    val chapters: List<Chapter>
)

data class Chapter(
    val number: Int,
    val verses: Int
)

object BibleBooks {
    val allBooks = listOf(
        BibleBook(1, "Genesis", listOf(
            Chapter(1,31), Chapter(2,25), Chapter(3,24), Chapter(4,26), Chapter(5,32),
            Chapter(6,22), Chapter(7,24), Chapter(8,22), Chapter(9,29), Chapter(10,32),
            Chapter(11,32), Chapter(12,20), Chapter(13,18), Chapter(14,24), Chapter(15,21),
            Chapter(16,16), Chapter(17,27), Chapter(18,33), Chapter(19,38), Chapter(20,18),
            Chapter(21,34), Chapter(22,24), Chapter(23,20), Chapter(24,67), Chapter(25,34),
            Chapter(26,35), Chapter(27,46), Chapter(28,22), Chapter(29,35), Chapter(30,43),
            Chapter(31,55), Chapter(32,32), Chapter(33,20), Chapter(34,31), Chapter(35,29),
            Chapter(36,43), Chapter(37,36), Chapter(38,30), Chapter(39,23), Chapter(40,23),
            Chapter(41,57), Chapter(42,38), Chapter(43,34), Chapter(44,34), Chapter(45,28),
            Chapter(46,34), Chapter(47,31), Chapter(48,22), Chapter(49,33), Chapter(50,26)
        )),
        BibleBook(2, "Exodus", listOf(
            Chapter(1,22), Chapter(2,25), Chapter(3,22), Chapter(4,31), Chapter(5,23),
            Chapter(6,30), Chapter(7,25), Chapter(8,32), Chapter(9,35), Chapter(10,29),
            Chapter(11,10), Chapter(12,51), Chapter(13,22), Chapter(14,31), Chapter(15,27),
            Chapter(16,36), Chapter(17,16), Chapter(18,27), Chapter(19,25), Chapter(20,26),
            Chapter(21,36), Chapter(22,31), Chapter(23,33), Chapter(24,18), Chapter(25,40),
            Chapter(26,37), Chapter(27,21), Chapter(28,43), Chapter(29,46), Chapter(30,38),
            Chapter(31,18), Chapter(32,35), Chapter(33,23), Chapter(34,35), Chapter(35,35),
            Chapter(36,38), Chapter(37,29), Chapter(38,31), Chapter(39,43), Chapter(40,38)
        )),
        BibleBook(3, "Leviticus", listOf(
            Chapter(1,17), Chapter(2,16), Chapter(3,17), Chapter(4,35), Chapter(5,19),
            Chapter(6,30), Chapter(7,38), Chapter(8,36), Chapter(9,24), Chapter(10,20),
            Chapter(11,47), Chapter(12,8), Chapter(13,59), Chapter(14,57), Chapter(15,33),
            Chapter(16,34), Chapter(17,16), Chapter(18,30), Chapter(19,37), Chapter(20,27),
            Chapter(21,24), Chapter(22,33), Chapter(23,44), Chapter(24,23), Chapter(25,55),
            Chapter(26,46), Chapter(27,34)
        )),
        BibleBook(4, "Numbers", listOf(
            Chapter(1,54), Chapter(2,34), Chapter(3,51), Chapter(4,49), Chapter(5,31),
            Chapter(6,27), Chapter(7,89), Chapter(8,26), Chapter(9,23), Chapter(10,36),
            Chapter(11,35), Chapter(12,16), Chapter(13,33), Chapter(14,45), Chapter(15,41),
            Chapter(16,50), Chapter(17,13), Chapter(18,32), Chapter(19,22), Chapter(20,29),
            Chapter(21,35), Chapter(22,41), Chapter(23,30), Chapter(24,25), Chapter(25,18),
            Chapter(26,65), Chapter(27,23), Chapter(28,31), Chapter(29,40), Chapter(30,16),
            Chapter(31,54), Chapter(32,42), Chapter(33,56), Chapter(34,29), Chapter(35,34),
            Chapter(36,13)
        )),
        BibleBook(5, "Deuteronomy", listOf(
            Chapter(1,46), Chapter(2,37), Chapter(3,29), Chapter(4,49), Chapter(5,33),
            Chapter(6,25), Chapter(7,26), Chapter(8,20), Chapter(9,29), Chapter(10,22),
            Chapter(11,32), Chapter(12,32), Chapter(13,18), Chapter(14,29), Chapter(15,23),
            Chapter(16,22), Chapter(17,20), Chapter(18,22), Chapter(19,21), Chapter(20,20),
            Chapter(21,23), Chapter(22,30), Chapter(23,25), Chapter(24,22), Chapter(25,19),
            Chapter(26,19), Chapter(27,68), Chapter(28,68), Chapter(29,27), Chapter(30,30),
            Chapter(31,25), Chapter(32,31), Chapter(33,29), Chapter(34,12)
        )),
        // Continue adding all remaining books (Joshua through Revelation)
        // You can copy the full chapter/verse counts from an authoritative source
    )
}
