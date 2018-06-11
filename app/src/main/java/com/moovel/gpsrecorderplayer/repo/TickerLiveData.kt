package com.moovel.gpsrecorderplayer.repo

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
