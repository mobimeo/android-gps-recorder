package com.moovel.gpsrecorderplayer.repo

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query

@Dao
internal interface LocationsDao : BaseDao<LocationStamp> {
    @Query("SELECT * from locations")
    fun get(): List<LocationStamp>

    @Query("SELECT * from locations WHERE record_id == :recordId")
    fun getByRecordId(recordId: String): List<LocationStamp>

    @Query("SELECT * from locations WHERE record_id == :recordId AND `index` == :index")
    fun getByRecordIdAndId(recordId: String, index: Long): LocationStamp?
}
