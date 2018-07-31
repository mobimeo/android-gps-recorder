package com.moovel.gpsrecorderplayer.utils

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue


fun Number.dpToPx(): Int = (toFloat() * Resources.getSystem().displayMetrics.density + .5f).toInt()

fun Number.format(digits: Int = 1): String = java.lang.String.format("%.${digits}f", this)

fun Context.primaryTextColor(): Int {
    val typedValue = TypedValue()
    val theme = theme
    theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)
    val arr = obtainStyledAttributes(typedValue.data, intArrayOf(android.R.attr.textColorPrimary))
    val primaryTextColor = arr.getColor(0, -1)
    arr.recycle()
    return primaryTextColor
}

fun <T1, T2, R> notNull(o1: T1?, o2: T2?, block: (T1, T2) -> R): R? =
        if (o1 != null && o2 != null) block(o1, o2) else null
