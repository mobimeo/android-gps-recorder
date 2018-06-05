package com.moovel.gpsrecorderplayer.repo

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
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
        db.locationsDao().insert((0..99).map { location("id1", it) })

        // get
        assertTrue(db.recordsDao().get().isNotEmpty())
        assertEquals(1, db.recordsDao().get().size)
        assertTrue(db.locationsDao().get().isNotEmpty())
        assertEquals(100, db.locationsDao().get().size)

        // remove
        db.recordsDao().delete(db.recordsDao().get())
        db.locationsDao().delete(db.locationsDao().get())

        assertTrue(db.recordsDao().get().isEmpty())
        assertTrue(db.locationsDao().get().isEmpty())
    }

    @Test
    fun testGetPolyline() {
        // add
        val latLng = Position(10.0, 45.0)
        db.recordsDao().insert(Record("id1", "first"))
        db.locationsDao().insert((0..99).map { location("id1", it, latLng) })

        // get
        val polyline = db.locationsDao().getPolyline("id1")

        // test
        assertEquals(100, polyline.size)
        polyline.forEach { assertEquals(latLng, it) }
    }

    private fun location(recordId: String, index: Int, latLng: Position = Position(48.45, 7.54)): LocationStamp {
        return LocationStamp(recordId,
                index,
                System.currentTimeMillis(),
                "mock",
                System.currentTimeMillis(),
                System.nanoTime(),
                latLng.latitude,
                latLng.longitude)
    }
}
