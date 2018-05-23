package com.moovel.gpsrecorderplayer.repo

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query

@Dao
internal interface PositionsDao : BaseDao<Position> {
    @Query("SELECT * from positions")
    fun get(): List<Position>

    @Query("SELECT * from positions WHERE record_id == :recordId")
    fun getByRecordId(recordId: String): List<Position>

    @Query("SELECT * from positions WHERE record_id == :recordId AND `index` == :index")
    fun getByRecordIdAndId(recordId: String, index: Long): List<Position>
}
