package com.moovel.gpsrecorderplayer.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
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
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_holder)
        when {
            bottom_drawer?.isOpen() == true -> bottom_drawer.close()
            (fragment as? BackPressable)?.onBackOrHomePress() == true -> return
            fragment is RecordsFragment -> super.onBackPressed()
            else -> startRecordsFragment()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_holder)
        return when {
            (fragment as? BackPressable)?.onBackOrHomePress() == true -> true
            item.itemId == android.R.id.home -> {
                startRecordsFragment()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    fun startRecordFragment() {
        startFragment("record", { RecordFragment() })
    }

    fun startPlaybackFragment(bundle: Bundle) {
        startFragment("play", { PlayBackFragment() }, bundle)
    }

    fun startRecordsFragment() {
        startFragment("list", { RecordsFragment() })
    }

    private fun startFragment(tag: String, factory: () -> Fragment, args: Bundle? = null) {
        val fragment = supportFragmentManager.findFragmentByTag(tag) ?: factory()

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_holder, fragment.apply { arguments = args }, tag)
                .commit()
    }

    fun enableBackButton(enable: Boolean) {
        supportActionBar?.setDisplayHomeAsUpEnabled(enable)
        supportActionBar?.setDisplayShowHomeEnabled(enable)
    }
}
