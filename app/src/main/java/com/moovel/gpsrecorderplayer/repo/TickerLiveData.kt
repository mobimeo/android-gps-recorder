package com.moovel.gpsrecorderplayer.repo

import android.arch.lifecycle.LiveData
import android.os.Handler
import android.os.SystemClock

internal class TickerLiveData : LiveData<Long>() {
    private val handler = Handler()
    private var running = false
    private var base = 0L

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
        val seconds = (SystemClock.elapsedRealtime() - base) / 1_000
        value = seconds
    }
}