package com.moovel.gpsrecorderplayer

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.moovel.gpsrecorderplayer.ui.records.RecordsFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, RecordsFragment.newInstance())
                    .commitNow()
        }
    }

}
