package com.moovel.gpsrecorderplayer.repo

import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult

internal class LocationLiveData(context: Context) : LiveData<Location>() {
    companion object {
        private val locationRequest: LocationRequest
            get() {
                return LocationRequest.create()
                        .setFastestInterval(1_000)
                        .setInterval(1_000)
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            }
    }

    private val client = FusedLocationProviderClient(context.applicationContext)

    override fun onActive() {
        client.lastLocation.addOnSuccessListener { it?.let {onLocation(it, true)} }
        client.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    override fun onInactive() {
        client.removeLocationUpdates(locationCallback)
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            onLocation(result.lastLocation, false)
        }
    }

    private fun onLocation(location: Location, last: Boolean) {
        if (!last || value == null) onLocation(location)
    }

    private fun onLocation(location: Location) {
        value = location
    }
}
