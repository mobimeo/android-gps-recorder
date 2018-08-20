/**
 * Copyright (c) 2010-2018 Moovel Group GmbH - moovel.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

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
        private var instance: RecordsDatabase? = null

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
