package com.moovel.gpsrecorderplayer.repo

import android.location.Location
import androidx.lifecycle.LiveData

interface LocationSource {
    fun locations(): LiveData<Location>
}