package com.moovel.gpsrecorderplayer.repo

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

@Database(version = 3,
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
                    instance = Room.databaseBuilder(context, RecordsDatabase::class.java, "db")
                            .fallbackToDestructiveMigration() // FIXME remove after schema is fixed
                            .build()
                }

                return instance!!
            }
        }
    }
}
