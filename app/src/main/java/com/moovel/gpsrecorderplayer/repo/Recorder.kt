package com.moovel.gpsrecorderplayer.repo

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import java.util.UUID

class Recorder internal constructor(
        context: Context,
        db: RecordsDatabase,
        val name: String
) : LocationSource {
    companion object {
        private val locationRequest: LocationRequest
            get() {
                return LocationRequest.create()
                        .setFastestInterval(1_000)
                        .setInterval(1_000)
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            }

        private fun Location.toPosition(
                recordId: String,
                index: Long,
                created: Long = System.currentTimeMillis()
        ): Position {
            return Position(recordId,
                    index,
                    created,
                    provider,
                    time,
                    elapsedRealtimeNanos,
                    latitude,
                    longitude,
                    if (hasAltitude()) altitude else null,
                    if (hasSpeed()) speed else null,
                    if (hasBearing()) bearing else null,
                    if (hasAccuracy()) accuracy else null,
                    if (hasVerticalAccuracy()) verticalAccuracyMeters else null,
                    if (hasSpeedAccuracy()) speedAccuracyMetersPerSecond else null,
                    if (hasBearingAccuracy()) bearingAccuracyDegrees else null
            )
        }
    }

    private val recordsDao = db.recordsDao()
    private val positionsDao = db.positionsDao()

    private val locationLiveData = MutableLiveData<Location>()

    val recordId = UUID.randomUUID().toString()
    private var index: Long = 0

    var completed: Boolean = false
        private set

    private val client = FusedLocationProviderClient(context.applicationContext)

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            onLocation(result.lastLocation, false)
        }
    }

    init {
        client.lastLocation.addOnSuccessListener { onLocation(it, true) }
        client.requestLocationUpdates(locationRequest, locationCallback, null)

        async {
            recordsDao.insert(Record(recordId, name))
        }
    }

    private fun onLocation(location: Location, last: Boolean) {
        if (!last || index == 0L) onLocation(location)
    }

    private fun onLocation(location: Location) {
        if (completed) return
        val currentIndex = index++
        async {
            val position = location.toPosition(recordId, currentIndex)
            positionsDao.insert(position)
        }
        locationLiveData.value = location
    }

    override fun locations(): LiveData<Location> {
        return locationLiveData
    }

    fun complete() {
        completed = true
        client.removeLocationUpdates(locationCallback)
    }
}