package com.moovel.gpsrecorderplayer.ui.widget

import android.content.Context
import android.location.Location
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.moovel.gpsrecorderplayer.R
import kotlinx.android.synthetic.main.view_location.view.*

class LocationView @JvmOverloads constructor(
        ctx: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        defStyleRes: Int = 0) :
        FrameLayout(ctx, attrs, defStyleAttr, defStyleRes) {

    var location: Location? = null
        set(value) {
            field = value
            if (value == null) return

            text_latitude.text = String.format("%.5f", value.latitude)
            text_longitude.text = String.format("%.5f", value.longitude)
            text_altitude.text = String.format("%.1f", value.altitude)
            text_accuracy.text = String.format("%.1f", value.accuracy)
            text_bearing.text = String.format("%.1f", value.bearing)
            text_bearing_accuracy.text = String.format("%.1f", value.bearingAccuracyDegrees)
            text_vertical_accuracy.text = String.format("%.1f", value.verticalAccuracyMeters)
            text_speed.text = String.format("%.1f", value.speed)
            text_speed_accuracy.text = String.format("%.1f", value.speedAccuracyMetersPerSecond)
        }

    init {
        View.inflate(ctx, R.layout.view_location, this)
    }
}