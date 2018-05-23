package com.moovel.gpsrecorderplayer.ui.records

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import com.moovel.gpsrecorderplayer.repo.RecordsService

class RecordsViewModel(application: Application) : AndroidViewModel(application) {
    val records = RecordsService.getInstance(application).records()
}
