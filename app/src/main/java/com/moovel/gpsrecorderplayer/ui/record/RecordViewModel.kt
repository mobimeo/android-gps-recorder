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
import com.moovel.gpsrecorderplayer.repo.RecordService

class RecordViewModel(application: Application) : AndroidViewModel(application) {

    val locationLiveData: LiveData<Location> = MediatorLiveData<Location>()

    init {
        val recordServiceIntent = Intent(application.applicationContext, RecordService::class.java)
        application.bindService(recordServiceIntent, object : ServiceConnection {
            override fun onServiceDisconnected(p0: ComponentName?) {}
            override fun onServiceConnected(p0: ComponentName, binder: IBinder) {
                val live = locationLiveData as MediatorLiveData<Location>
                live.addSource(RecordService.of(binder).locations(), { value -> live.value = value })
            }
        }, BIND_AUTO_CREATE)
    }
}
