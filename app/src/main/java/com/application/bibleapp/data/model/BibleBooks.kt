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
        // OLD TESTAMENT
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
            Chapter(26,19), Chapter(27,26), Chapter(28,68), Chapter(29,29), Chapter(30,20),
            Chapter(31,30), Chapter(32,52), Chapter(33,29), Chapter(34,12)
        )),
        BibleBook(6, "Joshua", listOf(
            Chapter(1,18), Chapter(2,24), Chapter(3,17), Chapter(4,24), Chapter(5,15),
            Chapter(6,27), Chapter(7,26), Chapter(8,35), Chapter(9,27), Chapter(10,43),
            Chapter(11,23), Chapter(12,24), Chapter(13,33), Chapter(14,15), Chapter(15,63),
            Chapter(16,10), Chapter(17,18), Chapter(18,28), Chapter(19,51), Chapter(20,9),
            Chapter(21,45), Chapter(22,34), Chapter(23,16), Chapter(24,33)
        )),
        BibleBook(7, "Judges", listOf(
            Chapter(1,36), Chapter(2,23), Chapter(3,31), Chapter(4,24), Chapter(5,31),
            Chapter(6,40), Chapter(7,25), Chapter(8,35), Chapter(9,57), Chapter(10,18),
            Chapter(11,40), Chapter(12,15), Chapter(13,25), Chapter(14,20), Chapter(15,20),
            Chapter(16,31), Chapter(17,13), Chapter(18,31), Chapter(19,30), Chapter(20,48),
            Chapter(21,25)
        )),
        BibleBook(8, "Ruth", listOf(
            Chapter(1,22), Chapter(2,23), Chapter(3,18), Chapter(4,22)
        )),
        BibleBook(9, "1 Samuel", listOf(
            Chapter(1,28), Chapter(2,36), Chapter(3,21), Chapter(4,22), Chapter(5,12),
            Chapter(6,21), Chapter(7,17), Chapter(8,22), Chapter(9,27), Chapter(10,27),
            Chapter(11,15), Chapter(12,25), Chapter(13,23), Chapter(14,52), Chapter(15,35),
            Chapter(16,23), Chapter(17,58), Chapter(18,30), Chapter(19,24), Chapter(20,42),
            Chapter(21,15), Chapter(22,23), Chapter(23,29), Chapter(24,22), Chapter(25,44),
            Chapter(26,25), Chapter(27,12), Chapter(28,25), Chapter(29,11), Chapter(30,31),
            Chapter(31,13)
        )),
        BibleBook(10, "2 Samuel", listOf(
            Chapter(1,27), Chapter(2,32), Chapter(3,39), Chapter(4,12), Chapter(5,25),
            Chapter(6,23), Chapter(7,29), Chapter(8,18), Chapter(9,13), Chapter(10,19),
            Chapter(11,27), Chapter(12,31), Chapter(13,39), Chapter(14,33), Chapter(15,37),
            Chapter(16,23), Chapter(17,29), Chapter(18,33), Chapter(19,43), Chapter(20,26),
            Chapter(21,22), Chapter(22,51), Chapter(23,39), Chapter(24,25)
        )),
        BibleBook(11, "1 Kings", listOf(
            Chapter(1,53), Chapter(2,46), Chapter(3,28), Chapter(4,34), Chapter(5,18),
            Chapter(6,38), Chapter(7,51), Chapter(8,66), Chapter(9,28), Chapter(10,29),
            Chapter(11,43), Chapter(12,33), Chapter(13,34), Chapter(14,31), Chapter(15,34),
            Chapter(16,34), Chapter(17,24), Chapter(18,46), Chapter(19,21), Chapter(20,43),
            Chapter(21,29), Chapter(22,53)
        )),
        BibleBook(12, "2 Kings", listOf(
            Chapter(1,18), Chapter(2,25), Chapter(3,27), Chapter(4,44), Chapter(5,27),
            Chapter(6,33), Chapter(7,20), Chapter(8,29), Chapter(9,37), Chapter(10,36),
            Chapter(11,21), Chapter(12,21), Chapter(13,25), Chapter(14,29), Chapter(15,38),
            Chapter(16,20), Chapter(17,41), Chapter(18,37), Chapter(19,37), Chapter(20,21),
            Chapter(21,26), Chapter(22,20), Chapter(23,37), Chapter(24,20), Chapter(25,30)
        )),
        BibleBook(13, "1 Chronicles", listOf(
            Chapter(1,54), Chapter(2,55), Chapter(3,24), Chapter(4,43), Chapter(5,26),
            Chapter(6,81), Chapter(7,40), Chapter(8,40), Chapter(9,44), Chapter(10,14),
            Chapter(11,47), Chapter(12,40), Chapter(13,14), Chapter(14,17), Chapter(15,29),
            Chapter(16,43), Chapter(17,27), Chapter(18,17), Chapter(19,19), Chapter(20,8),
            Chapter(21,30), Chapter(22,19), Chapter(23,32), Chapter(24,31), Chapter(25,31),
            Chapter(26,32), Chapter(27,34), Chapter(28,21), Chapter(29,30)
        )),
        BibleBook(14, "2 Chronicles", listOf(
            Chapter(1,17), Chapter(2,18), Chapter(3,17), Chapter(4,22), Chapter(5,14),
            Chapter(6,42), Chapter(7,22), Chapter(8,18), Chapter(9,31), Chapter(10,19),
            Chapter(11,23), Chapter(12,16), Chapter(13,22), Chapter(14,15), Chapter(15,19),
            Chapter(16,14), Chapter(17,19), Chapter(18,34), Chapter(19,11), Chapter(20,37),
            Chapter(21,20), Chapter(22,12), Chapter(23,21), Chapter(24,27), Chapter(25,28),
            Chapter(26,23), Chapter(27,9), Chapter(28,27), Chapter(29,36), Chapter(30,27),
            Chapter(31,21), Chapter(32,33), Chapter(33,25), Chapter(34,33), Chapter(35,27),
            Chapter(36,23)
        )),
        BibleBook(15, "Ezra", listOf(
            Chapter(1,11), Chapter(2,70), Chapter(3,13), Chapter(4,24), Chapter(5,17),
            Chapter(6,22), Chapter(7,28), Chapter(8,36), Chapter(9,15), Chapter(10,44)
        )),
        BibleBook(16, "Nehemiah", listOf(
            Chapter(1,11), Chapter(2,20), Chapter(3,32), Chapter(4,23), Chapter(5,19),
            Chapter(6,19), Chapter(7,73), Chapter(8,18), Chapter(9,38), Chapter(10,39),
            Chapter(11,36), Chapter(12,47), Chapter(13,31)
        )),
        BibleBook(17, "Esther", listOf(
            Chapter(1,22), Chapter(2,23), Chapter(3,15), Chapter(4,17), Chapter(5,14),
            Chapter(6,14), Chapter(7,10), Chapter(8,17), Chapter(9,32), Chapter(10,3)
        )),
        BibleBook(18, "Job", listOf(
            Chapter(1,22), Chapter(2,13), Chapter(3,26), Chapter(4,21), Chapter(5,27),
            Chapter(6,30), Chapter(7,21), Chapter(8,22), Chapter(9,35), Chapter(10,22),
            Chapter(11,20), Chapter(12,25), Chapter(13,28), Chapter(14,22), Chapter(15,35),
            Chapter(16,22), Chapter(17,16), Chapter(18,21), Chapter(19,29), Chapter(20,29),
            Chapter(21,34), Chapter(22,30), Chapter(23,17), Chapter(24,25), Chapter(25,6),
            Chapter(26,14), Chapter(27,23), Chapter(28,28), Chapter(29,25), Chapter(30,31),
            Chapter(31,40), Chapter(32,22), Chapter(33,33), Chapter(34,37), Chapter(35,16),
            Chapter(36,33), Chapter(37,24), Chapter(38,41), Chapter(39,30), Chapter(40,24),
            Chapter(41,34), Chapter(42,17)
        )),
        BibleBook(19, "Psalms", listOf(
            Chapter(1,6), Chapter(2,12), Chapter(3,8), Chapter(4,8), Chapter(5,12),
            Chapter(6,10), Chapter(7,17), Chapter(8,9), Chapter(9,20), Chapter(10,18),
            Chapter(11,7), Chapter(12,8), Chapter(13,6), Chapter(14,7), Chapter(15,5),
            Chapter(16,11), Chapter(17,15), Chapter(18,50), Chapter(19,14), Chapter(20,9),
            Chapter(21,13), Chapter(22,31), Chapter(23,6), Chapter(24,10), Chapter(25,22),
            Chapter(26,12), Chapter(27,14), Chapter(28,9), Chapter(29,11), Chapter(30,12),
            Chapter(31,24), Chapter(32,11), Chapter(33,22), Chapter(34,22), Chapter(35,28),
            Chapter(36,12), Chapter(37,40), Chapter(38,22), Chapter(39,13), Chapter(40,17),
            Chapter(41,13), Chapter(42,11), Chapter(43,5), Chapter(44,26), Chapter(45,17),
            Chapter(46,11), Chapter(47,9), Chapter(48,14), Chapter(49,20), Chapter(50,23),
            Chapter(51,19), Chapter(52,9), Chapter(53,6), Chapter(54,7), Chapter(55,23),
            Chapter(56,13), Chapter(57,11), Chapter(58,11), Chapter(59,17), Chapter(60,12),
            Chapter(61,8), Chapter(62,12), Chapter(63,11), Chapter(64,10), Chapter(65,13),
            Chapter(66,20), Chapter(67,7), Chapter(68,35), Chapter(69,36), Chapter(70,5),
            Chapter(71,24), Chapter(72,20), Chapter(73,28), Chapter(74,23), Chapter(75,10),
            Chapter(76,12), Chapter(77,20), Chapter(78,72), Chapter(79,13), Chapter(80,19),
            Chapter(81,16), Chapter(82,8), Chapter(83,18), Chapter(84,12), Chapter(85,13),
            Chapter(86,17), Chapter(87,7), Chapter(88,18), Chapter(89,52), Chapter(90,17),
            Chapter(91,16), Chapter(92,15), Chapter(93,5), Chapter(94,23), Chapter(95,11),
            Chapter(96,13), Chapter(97,12), Chapter(98,9), Chapter(99,9), Chapter(100,5),
            Chapter(101,8), Chapter(102,28), Chapter(103,22), Chapter(104,35), Chapter(105,45),
            Chapter(106,48), Chapter(107,43), Chapter(108,13), Chapter(109,31), Chapter(110,7),
            Chapter(111,10), Chapter(112,10), Chapter(113,9), Chapter(114,8), Chapter(115,18),
            Chapter(116,19), Chapter(117,2), Chapter(118,29), Chapter(119,176), Chapter(120,7),
            Chapter(121,8), Chapter(122,9), Chapter(123,4), Chapter(124,8), Chapter(125,5),
            Chapter(126,6), Chapter(127,5), Chapter(128,6), Chapter(129,8), Chapter(130,8),
            Chapter(131,3), Chapter(132,18), Chapter(133,3), Chapter(134,3), Chapter(135,21),
            Chapter(136,26), Chapter(137,9), Chapter(138,8), Chapter(139,24), Chapter(140,13),
            Chapter(141,10), Chapter(142,7), Chapter(143,12), Chapter(144,15), Chapter(145,21),
            Chapter(146,10), Chapter(147,20), Chapter(148,14), Chapter(149,9), Chapter(150,6)
        )),
        BibleBook(20, "Proverbs", listOf(
            Chapter(1,33), Chapter(2,22), Chapter(3,35), Chapter(4,27), Chapter(5,23),
            Chapter(6,35), Chapter(7,27), Chapter(8,36), Chapter(9,18), Chapter(10,32),
            Chapter(11,31), Chapter(12,28), Chapter(13,25), Chapter(14,35), Chapter(15,33),
            Chapter(16,33), Chapter(17,28), Chapter(18,24), Chapter(19,29), Chapter(20,30),
            Chapter(21,31), Chapter(22,29), Chapter(23,35), Chapter(24,34), Chapter(25,28),
            Chapter(26,28), Chapter(27,27), Chapter(28,28), Chapter(29,27), Chapter(30,33),
            Chapter(31,31)
        )),
        BibleBook(21, "Ecclesiastes", listOf(
            Chapter(1,18), Chapter(2,26), Chapter(3,22), Chapter(4,16), Chapter(5,20),
            Chapter(6,12), Chapter(7,29), Chapter(8,17), Chapter(9,18), Chapter(10,20),
            Chapter(11,10), Chapter(12,14)
        )),
        BibleBook(22, "Song of Solomon", listOf(
            Chapter(1,17), Chapter(2,17), Chapter(3,11), Chapter(4,16), Chapter(5,16),
            Chapter(6,13), Chapter(7,13), Chapter(8,14)
        )),
        BibleBook(23, "Isaiah", listOf(
            Chapter(1,31), Chapter(2,22), Chapter(3,26), Chapter(4,6), Chapter(5,30),
            Chapter(6,13), Chapter(7,25), Chapter(8,22), Chapter(9,21), Chapter(10,34),
            Chapter(11,16), Chapter(12,6), Chapter(13,22), Chapter(14,32), Chapter(15,9),
            Chapter(16,14), Chapter(17,14), Chapter(18,7), Chapter(19,25), Chapter(20,6),
            Chapter(21,17), Chapter(22,25), Chapter(23,18), Chapter(24,23), Chapter(25,12),
            Chapter(26,21), Chapter(27,13), Chapter(28,29), Chapter(29,24), Chapter(30,33),
            Chapter(31,9), Chapter(32,20), Chapter(33,24), Chapter(34,17), Chapter(35,10),
            Chapter(36,22), Chapter(37,38), Chapter(38,22), Chapter(39,8), Chapter(40,31),
            Chapter(41,29), Chapter(42,25), Chapter(43,28), Chapter(44,28), Chapter(45,25),
            Chapter(46,13), Chapter(47,15), Chapter(48,22), Chapter(49,26), Chapter(50,11),
            Chapter(51,23), Chapter(52,15), Chapter(53,12), Chapter(54,17), Chapter(55,11),
            Chapter(56,12), Chapter(57,21), Chapter(58,14), Chapter(59,21), Chapter(60,22),
            Chapter(61,11), Chapter(62,12), Chapter(63,19), Chapter(64,12), Chapter(65,25),
            Chapter(66,24)
        )),
        BibleBook(24, "Jeremiah", listOf(
            Chapter(1,19), Chapter(2,37), Chapter(3,25), Chapter(4,31), Chapter(5,31),
            Chapter(6,30), Chapter(7,34), Chapter(8,22), Chapter(9,26), Chapter(10,25),
            Chapter(11,23), Chapter(12,17), Chapter(13,27), Chapter(14,22), Chapter(15,21),
            Chapter(16,21), Chapter(17,27), Chapter(18,23), Chapter(19,15), Chapter(20,18),
            Chapter(21,14), Chapter(22,30), Chapter(23,40), Chapter(24,10), Chapter(25,38),
            Chapter(26,24), Chapter(27,22), Chapter(28,17), Chapter(29,32), Chapter(30,24),
            Chapter(31,40), Chapter(32,44), Chapter(33,26), Chapter(34,22), Chapter(35,19),
            Chapter(36,32), Chapter(37,21), Chapter(38,28), Chapter(39,18), Chapter(40,16),
            Chapter(41,18), Chapter(42,22), Chapter(43,13), Chapter(44,30), Chapter(45,5),
            Chapter(46,28), Chapter(47,7), Chapter(48,47), Chapter(49,39), Chapter(50,46),
            Chapter(51,64), Chapter(52,34)
        )),
        BibleBook(25, "Lamentations", listOf(
            Chapter(1,22), Chapter(2,22), Chapter(3,66), Chapter(4,22), Chapter(5,22)
        )),
        BibleBook(26, "Ezekiel", listOf(
            Chapter(1,28), Chapter(2,10), Chapter(3,27), Chapter(4,17), Chapter(5,17),
            Chapter(6,14), Chapter(7,27), Chapter(8,18), Chapter(9,11), Chapter(10,22),
            Chapter(11,25), Chapter(12,28), Chapter(13,23), Chapter(14,23), Chapter(15,8),
            Chapter(16,63), Chapter(17,24), Chapter(18,32), Chapter(19,14), Chapter(20,49),
            Chapter(21,32), Chapter(22,31), Chapter(23,49), Chapter(24,27), Chapter(25,17),
            Chapter(26,21), Chapter(27,36), Chapter(28,26), Chapter(29,21), Chapter(30,26),
            Chapter(31,18), Chapter(32,32), Chapter(33,33), Chapter(34,31), Chapter(35,15),
            Chapter(36,38), Chapter(37,28), Chapter(38,23), Chapter(39,29), Chapter(40,49),
            Chapter(41,26), Chapter(42,20), Chapter(43,27), Chapter(44,31), Chapter(45,25),
            Chapter(46,24), Chapter(47,23), Chapter(48,35)
        )),
        BibleBook(27, "Daniel", listOf(
            Chapter(1,21), Chapter(2,49), Chapter(3,30), Chapter(4,37), Chapter(5,31),
            Chapter(6,28), Chapter(7,28), Chapter(8,27), Chapter(9,27), Chapter(10,21),
            Chapter(11,45), Chapter(12,13)
        )),
        BibleBook(28, "Hosea", listOf(
            Chapter(1,11), Chapter(2,23), Chapter(3,5), Chapter(4,19), Chapter(5,15),
            Chapter(6,11), Chapter(7,16), Chapter(8,14), Chapter(9,17), Chapter(10,15),
            Chapter(11,12), Chapter(12,14), Chapter(13,16), Chapter(14,9)
        )),
        BibleBook(29, "Joel", listOf(
            Chapter(1,20), Chapter(2,32), Chapter(3,21)
        )),
        BibleBook(30, "Amos", listOf(
            Chapter(1,15), Chapter(2,16), Chapter(3,15), Chapter(4,13), Chapter(5,27),
            Chapter(6,14), Chapter(7,17), Chapter(8,14), Chapter(9,15)
        )),
        BibleBook(31, "Obadiah", listOf(
            Chapter(1,21)
        )),
        BibleBook(32, "Jonah", listOf(
            Chapter(1,17), Chapter(2,10), Chapter(3,10), Chapter(4,11)
        )),
        BibleBook(33, "Micah", listOf(
            Chapter(1,16), Chapter(2,13), Chapter(3,12), Chapter(4,13), Chapter(5,15),
            Chapter(6,16), Chapter(7,20)
        )),
        BibleBook(34, "Nahum", listOf(
            Chapter(1,15), Chapter(2,13), Chapter(3,19)
        )),
        BibleBook(35, "Habakkuk", listOf(
            Chapter(1,17), Chapter(2,20), Chapter(3,19)
        )),
        BibleBook(36, "Zephaniah", listOf(
            Chapter(1,18), Chapter(2,15), Chapter(3,20)
        )),
        BibleBook(37, "Haggai", listOf(
            Chapter(1,15), Chapter(2,23)
        )),
        BibleBook(38, "Zechariah", listOf(
            Chapter(1,21), Chapter(2,13), Chapter(3,10), Chapter(4,14), Chapter(5,11),
            Chapter(6,15), Chapter(7,14), Chapter(8,23), Chapter(9,17), Chapter(10,12),
            Chapter(11,17), Chapter(12,14), Chapter(13,9), Chapter(14,21)
        )),
        BibleBook(39, "Malachi", listOf(
            Chapter(1,14), Chapter(2,17), Chapter(3,18), Chapter(4,6)
        )),

        // NEW TESTAMENT
        BibleBook(40, "Matthew", listOf(
            Chapter(1,25), Chapter(2,23), Chapter(3,17), Chapter(4,25), Chapter(5,48),
            Chapter(6,34), Chapter(7,29), Chapter(8,34), Chapter(9,38), Chapter(10,42),
            Chapter(11,30), Chapter(12,50), Chapter(13,58), Chapter(14,36), Chapter(15,39),
            Chapter(16,28), Chapter(17,27), Chapter(18,35), Chapter(19,30), Chapter(20,34),
            Chapter(21,46), Chapter(22,46), Chapter(23,39), Chapter(24,51), Chapter(25,46),
            Chapter(26,75), Chapter(27,66), Chapter(28,20)
        )),
        BibleBook(41, "Mark", listOf(
            Chapter(1,45), Chapter(2,28), Chapter(3,35), Chapter(4,41), Chapter(5,43),
            Chapter(6,56), Chapter(7,37), Chapter(8,38), Chapter(9,50), Chapter(10,52),
            Chapter(11,33), Chapter(12,44), Chapter(13,37), Chapter(14,72), Chapter(15,47),
            Chapter(16,20)
        )),
        BibleBook(42, "Luke", listOf(
            Chapter(1,80), Chapter(2,52), Chapter(3,38), Chapter(4,44), Chapter(5,39),
            Chapter(6,49), Chapter(7,50), Chapter(8,56), Chapter(9,62), Chapter(10,42),
            Chapter(11,54), Chapter(12,59), Chapter(13,35), Chapter(14,35), Chapter(15,32),
            Chapter(16,31), Chapter(17,37), Chapter(18,43), Chapter(19,48), Chapter(20,47),
            Chapter(21,38), Chapter(22,71), Chapter(23,56), Chapter(24,53)
        )),
        BibleBook(43, "John", listOf(
            Chapter(1,51), Chapter(2,25), Chapter(3,36), Chapter(4,54), Chapter(5,47),
            Chapter(6,71), Chapter(7,53), Chapter(8,59), Chapter(9,41), Chapter(10,42),
            Chapter(11,57), Chapter(12,50), Chapter(13,38), Chapter(14,31), Chapter(15,27),
            Chapter(16,33), Chapter(17,26), Chapter(18,40), Chapter(19,42), Chapter(20,31),
            Chapter(21,25)
        )),
        BibleBook(44, "Acts", listOf(
            Chapter(1,26), Chapter(2,47), Chapter(3,26), Chapter(4,37), Chapter(5,42),
            Chapter(6,15), Chapter(7,60), Chapter(8,40), Chapter(9,43), Chapter(10,48),
            Chapter(11,30), Chapter(12,25), Chapter(13,52), Chapter(14,28), Chapter(15,41),
            Chapter(16,40), Chapter(17,34), Chapter(18,28), Chapter(19,41), Chapter(20,38),
            Chapter(21,40), Chapter(22,30), Chapter(23,35), Chapter(24,27), Chapter(25,27),
            Chapter(26,32), Chapter(27,44), Chapter(28,31)
        )),
        BibleBook(45, "Romans", listOf(
            Chapter(1,32), Chapter(2,29), Chapter(3,31), Chapter(4,25), Chapter(5,21),
            Chapter(6,23), Chapter(7,25), Chapter(8,39), Chapter(9,33), Chapter(10,21),
            Chapter(11,36), Chapter(12,21), Chapter(13,14), Chapter(14,23), Chapter(15,33),
            Chapter(16,27)
        )),
        BibleBook(46, "1 Corinthians", listOf(
            Chapter(1,31), Chapter(2,16), Chapter(3,23), Chapter(4,21), Chapter(5,13),
            Chapter(6,20), Chapter(7,40), Chapter(8,13), Chapter(9,27), Chapter(10,33),
            Chapter(11,34), Chapter(12,31), Chapter(13,13), Chapter(14,40), Chapter(15,58),
            Chapter(16,24)
        )),
        BibleBook(47, "2 Corinthians", listOf(
            Chapter(1,24), Chapter(2,17), Chapter(3,18), Chapter(4,18), Chapter(5,21),
            Chapter(6,18), Chapter(7,16), Chapter(8,24), Chapter(9,15), Chapter(10,18),
            Chapter(11,33), Chapter(12,21), Chapter(13,14)
        )),
        BibleBook(48, "Galatians", listOf(
            Chapter(1,24), Chapter(2,21), Chapter(3,29), Chapter(4,31), Chapter(5,26),
            Chapter(6,18)
        )),
        BibleBook(49, "Ephesians", listOf(
            Chapter(1,23), Chapter(2,22), Chapter(3,21), Chapter(4,32), Chapter(5,33),
            Chapter(6,24)
        )),
        BibleBook(50, "Philippians", listOf(
            Chapter(1,30), Chapter(2,30), Chapter(3,21), Chapter(4,23)
        )),
        BibleBook(51, "Colossians", listOf(
            Chapter(1,29), Chapter(2,23), Chapter(3,25), Chapter(4,18)
        )),
        BibleBook(52, "1 Thessalonians", listOf(
            Chapter(1,10), Chapter(2,20), Chapter(3,13), Chapter(4,18), Chapter(5,28)
        )),
        BibleBook(53, "2 Thessalonians", listOf(
            Chapter(1,12), Chapter(2,17), Chapter(3,18)
        )),
        BibleBook(54, "1 Timothy", listOf(
            Chapter(1,20), Chapter(2,15), Chapter(3,16), Chapter(4,16), Chapter(5,25),
            Chapter(6,21)
        )),
        BibleBook(55, "2 Timothy", listOf(
            Chapter(1,18), Chapter(2,26), Chapter(3,17), Chapter(4,22)
        )),
        BibleBook(56, "Titus", listOf(
            Chapter(1,16), Chapter(2,15), Chapter(3,15)
        )),
        BibleBook(57, "Philemon", listOf(
            Chapter(1,25)
        )),
        BibleBook(58, "Hebrews", listOf(
            Chapter(1,14), Chapter(2,18), Chapter(3,19), Chapter(4,16), Chapter(5,14),
            Chapter(6,20), Chapter(7,28), Chapter(8,13), Chapter(9,28), Chapter(10,39),
            Chapter(11,40), Chapter(12,29), Chapter(13,25)
        )),
        BibleBook(59, "James", listOf(
            Chapter(1,27), Chapter(2,26), Chapter(3,18), Chapter(4,17), Chapter(5,20)
        )),
        BibleBook(60, "1 Peter", listOf(
            Chapter(1,25), Chapter(2,25), Chapter(3,22), Chapter(4,19), Chapter(5,14)
        )),
        BibleBook(61, "2 Peter", listOf(
            Chapter(1,21), Chapter(2,22), Chapter(3,18)
        )),
        BibleBook(62, "1 John", listOf(
            Chapter(1,10), Chapter(2,29), Chapter(3,24), Chapter(4,21), Chapter(5,21)
        )),
        BibleBook(63, "2 John", listOf(
            Chapter(1,13)
        )),
        BibleBook(64, "3 John", listOf(
            Chapter(1,15)
        )),
        BibleBook(65, "Jude", listOf(
            Chapter(1,25)
        )),
        BibleBook(66, "Revelation", listOf(
            Chapter(1,20), Chapter(2,29), Chapter(3,22), Chapter(4,11), Chapter(5,14),
            Chapter(6,17), Chapter(7,17), Chapter(8,13), Chapter(9,21), Chapter(10,11),
            Chapter(11,19), Chapter(12,17), Chapter(13,18), Chapter(14,20), Chapter(15,8),
            Chapter(16,21), Chapter(17,18), Chapter(18,24), Chapter(19,21), Chapter(20,15),
            Chapter(21,27), Chapter(22,21)
        ))
    )

    /**
     * Get a book by its ID
     */
    fun getBookById(id: Int): BibleBook? = allBooks.find { it.id == id }

    /**
     * Get a book by its name (case-insensitive)
     */
    fun getBookByName(name: String): BibleBook? =
        allBooks.find { it.name.equals(name, ignoreCase = true) }

    /**
     * Get total number of chapters in a book
     */
    fun getChapterCount(bookId: Int): Int =
        getBookById(bookId)?.chapters?.size ?: 0

    /**
     * Get number of verses in a specific chapter
     */
    fun getVerseCount(bookId: Int, chapterNumber: Int): Int =
        getBookById(bookId)?.chapters?.find { it.number == chapterNumber }?.verses ?: 0

    /**
     * Validate if a book/chapter/verse reference exists
     */
    fun isValidReference(bookId: Int, chapterNumber: Int, verseNumber: Int): Boolean {
        val book = getBookById(bookId) ?: return false
        val chapter = book.chapters.find { it.number == chapterNumber } ?: return false
        return verseNumber in 1..chapter.verses
    }

    /**
     * Get Old Testament books (1-39)
     */
    val oldTestamentBooks = allBooks.filter { it.id in 1..39 }

    /**
     * Get New Testament books (40-66)
     */
    val newTestamentBooks = allBooks.filter { it.id in 40..66 }
}