package com.moovel.gpsrecorderplayer.ui.record

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.content.ComponentName
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import android.location.Location
import android.os.IBinder
import com.moovel.gpsrecorderplayer.repo.IRecordService
import com.moovel.gpsrecorderplayer.repo.Record
import com.moovel.gpsrecorderplayer.repo.RecordService

class RecordViewModel(application: Application) : AndroidViewModel(application) {

    val locationLiveData: LiveData<Location> = MediatorLiveData<Location>()
    val recordingLiveData: LiveData<Boolean> = MediatorLiveData<Boolean>()
    var stopListener: ((record: Record?) -> Unit)? = null

    private lateinit var service: IRecordService

    init {
        val recordServiceIntent = Intent(application.applicationContext, RecordService::class.java)
        application.bindService(recordServiceIntent, object : ServiceConnection {
            override fun onServiceDisconnected(p0: ComponentName?) {}
            override fun onServiceConnected(p0: ComponentName, binder: IBinder) {
                service = RecordService.of(binder)

                val live = locationLiveData as MediatorLiveData<Location>
                live.addSource(service.locations(), { value -> live.value = value })

                val recording = recordingLiveData as MediatorLiveData<Boolean>
                recording.addSource(service.isRecording(), { value -> recording.value = value })
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
