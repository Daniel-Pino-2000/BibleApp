package com.application.bibleapp.data.remote

import com.application.bibleapp.data.model.DailyVerseRef

/**
 * Abstracts wherever the Home screen's daily-verse reference comes from. Deliberately
 * separate from [BibleRemoteDataSource]: that interface's implementers are clients for
 * whichever provider serves full Bible-text downloads (currently bible.helloao.org),
 * while a daily-verse provider (currently OurManna) is a different API with a
 * different lifecycle — see [OurMannaBibleDataSource].
 */
interface DailyVerseDataSource {
    suspend fun getDailyVerse(): DailyVerseRef
}
