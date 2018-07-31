package com.moovel.gpsrecorderplayer.service

import androidx.lifecycle.LiveData
import com.moovel.gpsrecorderplayer.repo.Signal

interface SignalSource {
    fun signal(): LiveData<Signal?>
}
