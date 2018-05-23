package com.moovel.gpsrecorderplayer.repo

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert

@Dao
interface LocationsDao {
    @Insert
    fun insert(locations: LocationEntity)
}
