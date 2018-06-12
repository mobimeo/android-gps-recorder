package com.moovel.gpsrecorderplayer.repo

import androidx.lifecycle.LiveData

interface SignalSource {
    fun signal(): LiveData<Signal>
}
