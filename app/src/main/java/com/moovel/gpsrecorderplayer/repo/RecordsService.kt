package com.moovel.gpsrecorderplayer.repo

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Room
import android.content.Context
import android.support.annotation.RequiresPermission

class RecordsService(private val context: Context) {
    private val db by lazy {
        Room.databaseBuilder(context, RecordsDatabase::class.java, "db").build()
    }

    fun records(): LiveData<List<Record>> {
        return db.recordsDao().getAsLiveData()
    }

    @RequiresPermission(
            anyOf = ["android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"]
    )
    fun record(name: String): Recorder {
        return Recorder(context, db, name)
    }

}
