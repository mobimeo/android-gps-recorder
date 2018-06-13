package com.moovel.gpsrecorderplayer.ui.records

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import com.moovel.gpsrecorderplayer.repo.Exporter
import com.moovel.gpsrecorderplayer.repo.Record
import com.moovel.gpsrecorderplayer.repo.RecordsDatabase
import com.moovel.gpsrecorderplayer.repo.async

class RecordsViewModel(application: Application) : AndroidViewModel(application) {

    val records = RecordsDatabase.getInstance(application).recordsDao().getAsLiveData()

    fun remove(records: Iterable<Record>) {
        async {
            RecordsDatabase.getInstance(getApplication()).recordsDao().delete(records)
        }
    }

    fun export(records: Collection<Record>, result: (Intent?, Throwable?) -> Unit) {
        Exporter.export(getApplication(), records, result)
    }
}
