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
import com.moovel.gpsrecorderplayer.repo.RecordService
import com.moovel.gpsrecorderplayer.repo.Signal
import com.moovel.gpsrecorderplayer.utils.switchMap

class RecordViewModel(application: Application) : AndroidViewModel(application) {

    private val serviceLiveData: MutableLiveData<IRecordService?> = MutableLiveData()

    val locationLiveData: LiveData<Location> = serviceLiveData.switchMap { it?.locations() }
    val signalLiveData: LiveData<Signal> = serviceLiveData.switchMap { it?.signal() }
    val recordingLiveData: LiveData<Boolean> = serviceLiveData.switchMap { it?.recording() }
    val tickerLiveData: LiveData<Long?> = serviceLiveData.switchMap { it?.ticker() }
    val polyline: LiveData<List<LatLng>> = serviceLiveData.switchMap { it?.polyline() }

    private var service: IRecordService? = null
    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {
            serviceLiveData.value = null
        }

        override fun onServiceConnected(p0: ComponentName, binder: IBinder) {
            service = RecordService.of(binder)
            serviceLiveData.value = service
        }
    }

    init {
        application.bindService(Intent(application, RecordService::class.java), connection, BIND_AUTO_CREATE)
    }

    override fun onCleared() {
        getApplication<Application>().unbindService(connection)
    }

    fun onClickButton(recordName: String) {
        if (service?.isRecording() == true) {
            service?.rename(recordName)
        } else {
            service?.start(recordName)
        }
    }

    fun stop(recordName: String) {
        service?.rename(recordName)
        service?.stop()
    }

    fun isRecording() = service?.isRecording() == true
}
