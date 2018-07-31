package com.moovel.gpsrecorderplayer.ui

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.moovel.gpsrecorderplayer.R
import com.moovel.gpsrecorderplayer.repo.Record
import com.moovel.gpsrecorderplayer.service.PlayService
import com.moovel.gpsrecorderplayer.service.RecordService
import com.moovel.gpsrecorderplayer.ui.playback.PlayBackFragment
import com.moovel.gpsrecorderplayer.ui.record.RecordFragment
import com.moovel.gpsrecorderplayer.ui.records.RecordsFragment
import kotlinx.android.synthetic.main.records_fragment.*
import java.lang.ref.SoftReference

class MainActivity : AppCompatActivity() {

    private val fragments = mutableMapOf<String, SoftReference<Fragment>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) startRecordsFragment()

        bindService(Intent(application, RecordService::class.java), object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) = Unit

            override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                if (RecordService.of(binder).isRecording()) startRecordFragment()
                unbindService(this)
            }
        }, BIND_AUTO_CREATE)

        bindService(Intent(application, PlayService::class.java), object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) = Unit

            override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                val playService = PlayService.of(binder)
                val record = playService.current()
                record?.let { startPlaybackFragment(record) }
                unbindService(this)
            }
        }, BIND_AUTO_CREATE)
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_holder)
        when {
            bottom_drawer?.isOpen() == true -> bottom_drawer.close()
            (fragment as? BackPressable)?.onBackPressed() == true -> return
            fragment is RecordsFragment -> super.onBackPressed()
            else -> startRecordsFragment()
        }
    }

    fun startRecordFragment() {
        startFragment("record", { RecordFragment() })
    }

    fun startPlaybackFragment(record: Record) {
        startFragment("play", { PlayBackFragment() }, bundleOf("record" to record))
    }

    fun startRecordsFragment() {
        startFragment("list", { RecordsFragment() })
    }

    private fun startFragment(tag: String, factory: () -> Fragment, args: Bundle? = null) {
        val fragment = fragments[tag]?.get() ?: factory().also { fragments[tag] = SoftReference(it) }

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_holder, fragment.apply { arguments = args }, tag)
                .commit()
    }
}
