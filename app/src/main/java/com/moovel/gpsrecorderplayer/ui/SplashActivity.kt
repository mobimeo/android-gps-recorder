package com.moovel.gpsrecorderplayer.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onResume() {
        super.onResume()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}