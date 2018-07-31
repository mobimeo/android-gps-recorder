package com.moovel.gpsrecorderplayer.service

import android.location.Location
import androidx.lifecycle.LiveData

interface LocationSource {
    fun locations(): LiveData<Location>
}