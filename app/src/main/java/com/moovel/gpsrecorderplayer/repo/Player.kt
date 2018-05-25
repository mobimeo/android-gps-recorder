package com.moovel.gpsrecorderplayer.repo

import android.arch.lifecycle.LiveData
import android.location.Location

class Player : Task, LocationSource {
    override fun locations(): LiveData<Location> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
