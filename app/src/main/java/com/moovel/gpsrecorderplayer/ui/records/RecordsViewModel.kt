package com.moovel.gpsrecorderplayer.ui.records

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import com.moovel.gpsrecorderplayer.repo.RecordsDatabase

class RecordsViewModel(application: Application) : AndroidViewModel(application) {
    // FIXME use service
    val records = RecordsDatabase.getInstance(application).recordsDao().getAsLiveData()
}
