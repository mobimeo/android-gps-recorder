package com.moovel.gpsrecorderplayer.utils

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.location.Location
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.model.LatLng

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
