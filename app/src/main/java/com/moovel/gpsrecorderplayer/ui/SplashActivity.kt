package com.moovel.gpsrecorderplayer.ui

import android.content.Intent
import android.support.v7.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onResume() {
        super.onResume()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}