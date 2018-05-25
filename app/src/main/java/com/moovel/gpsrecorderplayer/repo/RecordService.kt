package com.moovel.gpsrecorderplayer.repo

import android.app.Service
import android.arch.lifecycle.LiveData
import android.content.Intent
import android.os.Binder
import android.os.IBinder

class RecordsService : Service() {
    var task: Task? = null
        private set

    private val records by lazy { RecordsDatabase.getInstance(this).recordsDao().getAsLiveData() }

    fun recorder(): Recorder {
        return Recorder(this)
    }

    fun player(): Player {
        return Player()
    }

    fun records(): LiveData<List<Record>> = records

    override fun onBind(intent: Intent?): IBinder {
        return RecordBinder(this)
    }

    class RecordBinder(val service: RecordsService) : Binder()
}
