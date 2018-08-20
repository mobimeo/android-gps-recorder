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
