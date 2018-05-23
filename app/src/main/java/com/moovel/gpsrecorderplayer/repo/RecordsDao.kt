package com.moovel.gpsrecorderplayer.repo

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert

@Dao
interface RecordsDao {
    @Insert
    fun insert(records: Record)
}
