package com.moovel.gpsrecorderplayer.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.moovel.gpsrecorderplayer.R
import com.moovel.gpsrecorderplayer.ui.playback.PlayBackFragment
import com.moovel.gpsrecorderplayer.ui.record.RecordFragment
import com.moovel.gpsrecorderplayer.ui.records.RecordsFragment
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.records_fragment.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        setSupportActionBar(toolbar)
        if (savedInstanceState == null) startRecordsFragment()
    }

    override fun onBackPressed() {
        if (bottom_drawer?.isOpen() == true) {
            bottom_drawer.close()
        } else {
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            startRecordsFragment()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    fun startRecordFragment() {
        supportFragmentManager
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.fragment_holder, RecordFragment())
                .commit()
    }

    fun startPlaybackFragment(bundle: Bundle) {
        supportFragmentManager
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.fragment_holder, PlayBackFragment().apply { arguments = bundle })
                .commit()
    }

    private fun startRecordsFragment() {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_holder, RecordsFragment())
                .commit()
    }

    fun enableBackButton(enable: Boolean) {
        supportActionBar?.setDisplayHomeAsUpEnabled(enable)
        supportActionBar?.setDisplayShowHomeEnabled(enable)
    }
}
