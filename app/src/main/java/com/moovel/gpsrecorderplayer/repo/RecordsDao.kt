package com.moovel.gpsrecorderplayer.repo

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
internal interface RecordsDao : BaseDao<Record> {
    @Query("SELECT * from records")
    fun get(): List<Record>

    @Query("SELECT * from records")
    fun getAsLiveData(): LiveData<List<Record>>
}
