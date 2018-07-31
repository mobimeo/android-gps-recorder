package com.moovel.gpsrecorderplayer.utils

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import java.lang.Exception

fun GoogleMap.setLocationSource(source: LiveData<Location>) {
    setLocationSource(object : LocationSource, Observer<Location> {
        private var listener: LocationSource.OnLocationChangedListener? = null

        override fun onChanged(t: Location?) {
            listener?.onLocationChanged(t)
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
    } catch (e: Exception) {
        // can be ignored. Screen is too small to show the map
    }
}