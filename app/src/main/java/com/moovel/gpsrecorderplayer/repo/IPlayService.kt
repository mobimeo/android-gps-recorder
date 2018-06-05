package com.moovel.gpsrecorderplayer.repo

import android.arch.lifecycle.LiveData
import com.google.android.gms.maps.model.LatLng

interface IPlayService : LocationSource, SignalSource {
    fun start(record: Record)
    fun stop()
    fun current(): Record?
    fun isPlaying(): LiveData<Boolean>
    fun polyline(): LiveData<List<LatLng>>
}
