package com.moovel.gpsrecorderplayer.ui.widget

import android.content.Context
import android.location.Location
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.moovel.gpsrecorderplayer.R
import com.moovel.gpsrecorderplayer.repo.Signal
import com.moovel.gpsrecorderplayer.utils.format
import kotlinx.android.synthetic.main.view_location.view.*

class LocationView @JvmOverloads constructor(
        ctx: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        defStyleRes: Int = 0) :
        FrameLayout(ctx, attrs, defStyleAttr, defStyleRes) {

    private val na = context.getString(R.string.record_not_available)

    var location: Location? = null
        set(value) {
            field = value
            latitude.text = value?.latitude?.format(4) ?: na
            longitude.text = value?.longitude?.format(4) ?: na
            speed.text = value?.speed?.takeIf { value.hasSpeed() }?.format() ?: na
            bearing.text = value?.bearing?.takeIf { value.hasBearing() }?.format() ?: na
            altitude.text = value?.altitude?.takeIf { value.hasAltitude() }?.format() ?: na
            accuracy.text = value?.accuracy?.takeIf { value.hasAccuracy() }?.format() ?: na
            bearing_acc.text = value?.bearingAccuracyDegrees?.takeIf { value.hasBearingAccuracy() }?.format() ?: na
            vertical_acc.text = value?.verticalAccuracyMeters?.takeIf { value.hasVerticalAccuracy() }?.format() ?: na
            speed_acc.text = value?.speedAccuracyMetersPerSecond?.takeIf { value.hasSpeedAccuracy() }?.format() ?: na
        }

    var signal: Signal? = null
        set(value) {
            field = value
            signal_type.text = value?.networkTypeName ?: na
            signal_class.text = value?.networkClassName ?: na
            signal_strength.text = value?.levelName ?: na
        }

    init {
        View.inflate(ctx, R.layout.view_location, this)
    }
}