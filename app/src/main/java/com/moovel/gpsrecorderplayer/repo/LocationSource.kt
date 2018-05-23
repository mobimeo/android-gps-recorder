package com.moovel.gpsrecorderplayer.repo

import android.arch.lifecycle.LiveData
import android.location.Location

interface LocationSource {
    fun locations(): LiveData<Location>
}