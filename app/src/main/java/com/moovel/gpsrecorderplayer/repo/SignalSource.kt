package com.moovel.gpsrecorderplayer.repo

import android.arch.lifecycle.LiveData

interface SignalSource {
    fun signal(): LiveData<Signal>
}
