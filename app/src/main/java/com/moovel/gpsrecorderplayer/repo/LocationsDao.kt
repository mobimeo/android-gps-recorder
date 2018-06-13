package com.moovel.gpsrecorderplayer.repo

import androidx.room.Dao
import androidx.room.Query

@Dao
internal interface LocationsDao : BaseDao<LocationStamp> {
    @Query("SELECT * from locations")
    fun get(): List<LocationStamp>

    @Query("SELECT * from locations WHERE record_id = :recordId ORDER BY `index`")
    fun getByRecordId(recordId: String): List<LocationStamp>

    @Query("SELECT * from locations WHERE record_id = :recordId AND `index` = :index")
    fun getByRecordIdAndIndex(recordId: String, index: Int): LocationStamp?

    @Query("SELECT latitude, longitude from locations WHERE record_id = :recordId")
    fun getPolyline(recordId: String): List<Position>
}
