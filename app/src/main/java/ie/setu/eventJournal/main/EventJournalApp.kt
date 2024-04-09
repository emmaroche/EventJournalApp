package ie.setu.eventJournal.main

import android.app.Application
import timber.log.Timber

class EventJournalApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        Timber.i("EventJournalApp Application Started")
    }
}