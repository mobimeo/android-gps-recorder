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
