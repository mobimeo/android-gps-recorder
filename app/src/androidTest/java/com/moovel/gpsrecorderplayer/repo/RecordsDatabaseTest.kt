package com.moovel.gpsrecorderplayer.repo

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecordsDatabaseTest {
    lateinit var db: RecordsDatabase

    @Before
    fun init() {
        db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(), RecordsDatabase::class.java).build()
    }

    @Test
    fun testAdd() {
        db.recordsDao().insert(Record("id1", "first"))
    }
}