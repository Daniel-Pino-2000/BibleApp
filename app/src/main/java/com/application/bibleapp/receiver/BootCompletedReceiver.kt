package com.application.bibleapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.application.bibleapp.ensureDailyVerseJobsScheduled

/**
 * Re-arms the daily-verse background jobs after the device reboots or this app is
 * updated. Both events can leave WorkManager's persisted schedule for the reminder
 * notification gone — a reboot clears in-memory JobScheduler/AlarmManager state that
 * WorkManager itself normally restores, but OEM battery managers (common on Xiaomi,
 * Samsung, Huawei, etc., especially under low-battery power saving) are known to
 * additionally cancel an app's scheduled jobs outright around boot or force-stop.
 * Without this receiver, a user who doesn't happen to open the app before their
 * reminder time on a given day would silently miss it. Being manifest-registered
 * (not just a runtime-registered receiver) is what lets the OS start this app's
 * process for these broadcasts even though the user hasn't opened it.
 *
 * [ensureDailyVerseJobsScheduled] is idempotent (KEEP policy), so this is safe to
 * call unconditionally without checking whether a schedule already exists.
 */
class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> ensureDailyVerseJobsScheduled(context)
        }
    }
}
