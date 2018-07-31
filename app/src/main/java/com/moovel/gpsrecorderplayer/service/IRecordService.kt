package com.moovel.gpsrecorderplayer.service

import androidx.lifecycle.LiveData
import com.google.android.gms.maps.model.LatLng
import com.moovel.gpsrecorderplayer.repo.Record

interface IRecordService : LocationSource, SignalSource {
    fun start(name: String): Record
    fun stop(): Record?
    fun current(): Record?
    fun rename(name: String)
    fun recording(): LiveData<Boolean>
    fun isRecording(): Boolean
    fun polyline(): LiveData<List<LatLng>>
    fun ticker(): LiveData<Long?>
}
