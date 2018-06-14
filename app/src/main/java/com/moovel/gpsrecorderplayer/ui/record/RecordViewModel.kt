package com.moovel.gpsrecorderplayer.ui.record

import android.app.Application
import android.content.ComponentName
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import android.location.Location
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.moovel.gpsrecorderplayer.repo.IRecordService
import com.moovel.gpsrecorderplayer.repo.Record
import com.moovel.gpsrecorderplayer.repo.RecordService
import com.moovel.gpsrecorderplayer.repo.Signal
import com.moovel.gpsrecorderplayer.utils.switchMap

class RecordViewModel(application: Application) : AndroidViewModel(application) {

    private val serviceLiveData: MutableLiveData<IRecordService?> = MutableLiveData()

    val locationLiveData: LiveData<Location> = serviceLiveData.switchMap { it?.locations() }
    val signalLiveData: LiveData<Signal> = serviceLiveData.switchMap { it?.signal() }
    val recordingLiveData: LiveData<Boolean> = serviceLiveData.switchMap { it?.isRecording() }
    val tickerLiveData: LiveData<Long?> = serviceLiveData.switchMap { it?.ticker() }
    val polyline: LiveData<List<LatLng>> = serviceLiveData.switchMap { it?.polyline() }
    var stopListener: ((record: Record?) -> Unit)? = null

    private lateinit var service: IRecordService

    init {
        val recordServiceIntent = Intent(application, RecordService::class.java)
        application.bindService(recordServiceIntent, object : ServiceConnection {
            override fun onServiceDisconnected(p0: ComponentName?) {
                serviceLiveData.value = null
            }

            override fun onServiceConnected(p0: ComponentName, binder: IBinder) {
                service = RecordService.of(binder)
                serviceLiveData.value = service
            }
        }, BIND_AUTO_CREATE)
    }

    fun onClickButton(recordName: String) {
        if (service.isRecording().value == true) {
            service.rename(recordName)
            stopListener?.invoke(service.stop())
        } else {
            service.start(recordName)
        }
    }
}
