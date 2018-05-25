package com.moovel.gpsrecorderplayer.repo

import android.app.Notification

internal interface TaskMediator {
    val id: Long

    fun notify(notification: Notification?)
}
