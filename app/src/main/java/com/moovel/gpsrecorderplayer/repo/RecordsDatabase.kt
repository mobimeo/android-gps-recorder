package com.moovel.gpsrecorderplayer.repo

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(version = 2,
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
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) = database.transaction {
                execSQL("ALTER TABLE records ADD COLUMN created INTEGER NOT NULL DEFAULT 0")
                execSQL("UPDATE records SET created = start")
            }
        }

        private fun <T> SupportSQLiteDatabase.transaction(block: SupportSQLiteDatabase.() -> T): T {
            beginTransaction()
            try {
                val result = block()
                setTransactionSuccessful()
                return result
            } finally {
                endTransaction()
            }
        }

        private var instance: RecordsDatabase? = null
        fun getInstance(context: Context): RecordsDatabase {
            if (instance != null) return instance!!
            synchronized(Companion) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.applicationContext, RecordsDatabase::class.java, "db")
                            .addMigrations(MIGRATION_1_2)
                            .build()
                }

                return instance!!
            }
        }
    }
}
