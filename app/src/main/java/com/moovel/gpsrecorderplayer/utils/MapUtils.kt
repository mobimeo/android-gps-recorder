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

package com.moovel.gpsrecorderplayer.utils

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import java.lang.IllegalArgumentException

fun GoogleMap.setLocationSource(source: LiveData<Location?>) {
    setLocationSource(object : LocationSource, Observer<Location?> {
        private var listener: LocationSource.OnLocationChangedListener? = null

        override fun onChanged(t: Location?) {
            notNull(listener, t) { cb, l -> cb.onLocationChanged(l) }
        }

        override fun deactivate() {
            source.removeObserver(this)
            listener = null
        }

        override fun activate(l: LocationSource.OnLocationChangedListener?) {
            listener = l
            source.observeForever(this)
        }
    })
}

val Location.latLng: LatLng get() = LatLng(latitude, longitude)

operator fun LatLng.component1() = latitude
operator fun LatLng.component2() = longitude

fun GoogleMap.zoomToPolyline(polyline: List<LatLng>) {
    val boundsBuilder = LatLngBounds.Builder()
    polyline.forEach { boundsBuilder.include(it) }
    try {
        moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 24.dpToPx()))
    } catch (e: IllegalStateException) {
        // FIXME Screen is too small to show the map
    }
}
