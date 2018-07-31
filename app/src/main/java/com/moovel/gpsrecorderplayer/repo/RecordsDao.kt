package com.moovel.gpsrecorderplayer.repo

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
internal interface RecordsDao : BaseDao<Record> {
    @Query("SELECT * from records ORDER by start DESC")
    fun get(): List<Record>

    @Query("SELECT * from records ORDER by start DESC")
    fun getAsLiveData(): LiveData<List<Record>>

    @Query("SELECT * from records WHERE id = :id")
    fun getById(id: String): Record?
}
