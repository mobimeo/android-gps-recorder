package com.moovel.gpsrecorderplayer.repo

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecordsDatabaseTest {
    private lateinit var db: RecordsDatabase

    @Before
    fun init() {
        db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(), RecordsDatabase::class.java).build()
    }

    @Test
    fun testAddGetRemove() {
        // add
        db.recordsDao().insert(Record("id1", "first"))
        db.positionsDao().insert((0..99).map { position("id1", it.toLong()) })

        // get
        assertTrue(db.recordsDao().get().isNotEmpty())
        assertEquals(1, db.recordsDao().get().size)
        assertTrue(db.positionsDao().get().isNotEmpty())
        assertEquals(100, db.positionsDao().get().size)

        // remove
        db.recordsDao().delete(db.recordsDao().get())
        db.positionsDao().delete(db.positionsDao().get())

        assertTrue(db.recordsDao().get().isEmpty())
        assertTrue(db.positionsDao().get().isEmpty())
    }

    private fun position(recordId: String, index: Long): Position {
        return Position(recordId,
                index,
                System.currentTimeMillis(),
                "mock",
                System.currentTimeMillis(),
                System.nanoTime(),
                48.45,
                7.54)
    }
}
