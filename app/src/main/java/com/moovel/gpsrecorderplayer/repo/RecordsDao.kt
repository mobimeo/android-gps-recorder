package com.moovel.gpsrecorderplayer.repo

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query

@Dao
internal interface RecordsDao : BaseDao<Record> {
    @Query("SELECT * from records")
    fun get(): List<Record>

    @Query("SELECT * from records")
    fun getAsLiveData(): LiveData<List<Record>>
}
