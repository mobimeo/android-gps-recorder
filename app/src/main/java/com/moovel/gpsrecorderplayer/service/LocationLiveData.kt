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

package com.moovel.gpsrecorderplayer.service

import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult

internal class LocationLiveData(context: Context) : LiveData<Location?>() {
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
        client.lastLocation.addOnSuccessListener { it?.let { onLocation(it, true) } }
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
