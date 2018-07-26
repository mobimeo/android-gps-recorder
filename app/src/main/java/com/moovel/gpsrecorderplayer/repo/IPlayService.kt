package com.moovel.gpsrecorderplayer.repo

import androidx.lifecycle.LiveData
import com.google.android.gms.maps.model.LatLng

interface IPlayService : LocationSource, SignalSource {
    fun initialize(record: Record)
    fun start()
    fun stop()
    fun ticker(): LiveData<Long?>
    fun current(): Record?
    fun isPlaying(): LiveData<Boolean>
    fun polyline(): LiveData<List<LatLng>>
}
