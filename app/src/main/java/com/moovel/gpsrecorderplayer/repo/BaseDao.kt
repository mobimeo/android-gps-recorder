package com.moovel.gpsrecorderplayer.repo

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update

internal interface BaseDao<T> {
    @Insert
    fun insert(vararg entities: T)

    @Insert
    fun insert(entities: Iterable<T>)

    @Update
    fun update(vararg entities: T)

    @Update
    fun update(entities: Iterable<T>)

    @Delete
    fun delete(vararg entities: T)

    @Delete
    fun delete(entities: Iterable<T>)
}
