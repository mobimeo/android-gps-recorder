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
        context: Context
) : Task, LocationSource {
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

    private val recordsDao = RecordsDatabase.getInstance(context).recordsDao()
    private val positionsDao = RecordsDatabase.getInstance(context).positionsDao()

    private val locationLiveData = MutableLiveData<Location>()

    val recordId = UUID.randomUUID().toString()
    private var index: Long = 0

    var state: State = State.READY
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
            recordsDao.insert(Record(recordId, recordId)) // FIXME name
        }
    }

    private fun onLocation(location: Location, last: Boolean) {
        if (!last || index == 0L) onLocation(location)
    }

    private fun onLocation(location: Location) {
        locationLiveData.value = location
        if (state != State.STARTED) return

        insertPosition(location)
    }

    private fun insertPosition(location: Location) {
        val currentIndex = index++
        async {
            val position = location.toPosition(recordId, currentIndex)
            positionsDao.insert(position)
        }
    }


    override fun locations(): LiveData<Location> {
        return locationLiveData
    }

    fun start() {
        if (state != State.READY) throw IllegalStateException("Recorder is finished")
        state = State.STARTED
        locationLiveData.value?.let { insertPosition(it) }
    }

    fun release() {
        if (state != State.STARTED || state != State.READY) throw IllegalStateException("Recorder already finished")
        state = State.RELEASED
        client.removeLocationUpdates(locationCallback)
    }

    @Deprecated("Lets remove it")
    enum class State {
        READY,
        STARTED,
        STOPPED,
        RELEASED
    }
}
