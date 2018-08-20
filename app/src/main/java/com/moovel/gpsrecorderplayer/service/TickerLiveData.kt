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

import android.os.Handler
import android.os.SystemClock
import androidx.lifecycle.LiveData

internal class TickerLiveData : LiveData<Long?>() {
    private val handler = Handler()
    private var running = false
    private var base: Long? = null

    init {
        value = null
    }

    fun reset() {
        val rerun = running
        stop()
        base = null
        update()
        if (rerun) start()
    }

    fun stop() {
        running = false
        update()
        handler.removeCallbacks(::dispatchTick)
    }

    fun start() {
        base = SystemClock.elapsedRealtime()
        running = true
        update()
        dispatchTick()
    }

    private fun dispatchTick() {
        update()
        if (running) handler.postDelayed(::dispatchTick, 1_000)
    }

    private fun update() {
        value = base?.let { (SystemClock.elapsedRealtime() - it) / 1_000 }
    }
}
