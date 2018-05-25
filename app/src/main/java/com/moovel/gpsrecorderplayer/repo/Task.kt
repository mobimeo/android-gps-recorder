package com.moovel.gpsrecorderplayer.repo

import android.content.Intent

interface Task {
    fun onAction(intent: Intent) = Unit
}
