package com.moovel.gpsrecorderplayer.utils

import android.content.res.Resources

fun Number.dpToPx(): Int = (toFloat() * Resources.getSystem().displayMetrics.density + .5f).toInt()