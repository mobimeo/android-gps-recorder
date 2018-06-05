package com.moovel.gpsrecorderplayer.repo

import android.arch.lifecycle.LiveData

class TickerLiveData : LiveData<Long>() {
    override fun onActive() {
        super.onActive()
    }

    override fun onInactive() {
        super.onInactive()
    }
}