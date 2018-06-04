package com.moovel.gpsrecorderplayer.repo

import android.arch.lifecycle.LiveData
import com.google.android.gms.maps.model.LatLng

interface IRecordService : LocationSource {
    fun start(name: String)
    fun stop()
    fun current(): Record?
    fun rename(name: String)
    fun isRecording(): Boolean {
        return current() != null
    }
    fun polyline(): LiveData<List<LatLng>>
}