package com.moovel.gpsrecorderplayer.repo

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(version = 1,
        entities = [
            Record::class,
            LocationStamp::class,
            SignalStamp::class
        ])
internal abstract class RecordsDatabase : RoomDatabase() {
    abstract fun recordsDao(): RecordsDao
    abstract fun locationsDao(): LocationsDao
    abstract fun signalsDao(): SignalsDao

    companion object {
        private var instance: RecordsDatabase? = null

        fun getInstance(context: Context): RecordsDatabase {
            if (instance != null) return instance!!
            synchronized(Companion) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context, RecordsDatabase::class.java, "db").build()
                }

                return instance!!
            }
        }
    }
}
