package com.moovel.gpsrecorderplayer.repo

import android.arch.lifecycle.LiveData
import com.google.android.gms.maps.model.LatLng

interface IRecordService : LocationSource, SignalSource {
    fun start(name: String)
    fun stop()
    fun current(): Record?
    fun rename(name: String)
    fun isRecording(): LiveData<Boolean>
    fun polyline(): LiveData<List<LatLng>>
}
