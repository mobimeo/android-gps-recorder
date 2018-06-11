package com.moovel.gpsrecorderplayer.repo

import androidx.lifecycle.LiveData
import com.google.android.gms.maps.model.LatLng

interface IRecordService : LocationSource, SignalSource {
    fun start(name: String)
    fun stop(): Record?
    fun current(): Record?
    fun rename(name: String)
    fun isRecording(): LiveData<Boolean>
    fun polyline(): LiveData<List<LatLng>>
    fun ticker(): LiveData<Long?>
}
