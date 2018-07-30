package com.moovel.gpsrecorderplayer.ui

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.moovel.gpsrecorderplayer.R
import com.moovel.gpsrecorderplayer.repo.PlayService
import com.moovel.gpsrecorderplayer.repo.RecordService
import com.moovel.gpsrecorderplayer.ui.playback.PlayBackFragment
import com.moovel.gpsrecorderplayer.ui.record.RecordFragment
import com.moovel.gpsrecorderplayer.ui.records.RecordsFragment
import kotlinx.android.synthetic.main.records_fragment.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) startRecordsFragment()

        application.bindService(Intent(application, RecordService::class.java), object : ServiceConnection {
            override fun onServiceDisconnected(p0: ComponentName?) {
            }

            override fun onServiceConnected(p0: ComponentName, binder: IBinder) {
                if (RecordService.of(binder).isRecording()) startRecordFragment()
                unbindService(this)
            }
        }, BIND_AUTO_CREATE)

        application.bindService(Intent(application, PlayService::class.java), object : ServiceConnection {
            override fun onServiceDisconnected(p0: ComponentName?) {
            }

            override fun onServiceConnected(p0: ComponentName, binder: IBinder) {
                val playService = PlayService.of(binder)
                if (playService.isPlaying()) startPlaybackFragment(Bundle().apply {
                    putParcelable("record", playService.current())
                })
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
}
