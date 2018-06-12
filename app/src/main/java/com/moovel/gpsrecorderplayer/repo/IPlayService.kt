package com.moovel.gpsrecorderplayer.repo

import androidx.lifecycle.LiveData
import com.google.android.gms.maps.model.LatLng

interface IPlayService : LocationSource, SignalSource {
    fun start(record: Record)
    fun stop()
    fun current(): Record?
    fun isPlaying(): LiveData<Boolean>
    fun polyline(): LiveData<List<LatLng>>
}
