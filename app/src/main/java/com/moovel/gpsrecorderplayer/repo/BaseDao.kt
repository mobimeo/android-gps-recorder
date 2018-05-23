package com.moovel.gpsrecorderplayer.repo

import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Update

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
