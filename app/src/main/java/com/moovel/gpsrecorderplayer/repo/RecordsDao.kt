package com.moovel.gpsrecorderplayer.repo

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query

@Dao
interface RecordsDao : BaseDao<Record> {
    @Query("SELECT * from records")
    fun get(): List<Record>
}
