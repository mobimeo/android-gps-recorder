package com.moovel.gpsrecorderplayer.repo

import android.app.Notification.VISIBILITY_PUBLIC
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_CANCEL_CURRENT
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.moovel.gpsrecorderplayer.R
import com.moovel.gpsrecorderplayer.ui.MainActivity

class PlayService : Service(), IPlayService {

    companion object {
        private const val NOTIFICATION_ID = 0x4654
        private const val NOTIFICATION_CHANNEL_ID = "play"
        private const val ACTION_STOP = "stop"

        fun of(binder: IBinder): IPlayService {
            return (binder as PlayBinder).service
        }
    }

    private val db by lazy { RecordsDatabase.getInstance(applicationContext) }
    private val client by lazy { FusedLocationProviderClient(applicationContext) }
    private val locationHandler by lazy {
        LocationHandler(db) { recordId: String, location: Location? ->
            if (recordId == current?.id) {
                if (location == null) {
                    stop()
                } else {
                    this.location.value = location
                }
            }
        }
    }

    private val intent by lazy {
        Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
    }
    private val pendingIntent by lazy {
        PendingIntent.getActivity(this, 0, intent, 0)
    }
    private val stopSelfIntent by lazy {
        Intent(this, PlayService::class.java).apply { action = ACTION_STOP }
    }
    private val selfStopPendingIntent by lazy {
        PendingIntent.getService(this, 0, stopSelfIntent, FLAG_CANCEL_CURRENT)
    }

    private val locationsDao by lazy { db.locationsDao() }
    private val location = MutableLiveData<Location>()
    private val playing = MutableLiveData<Boolean>()
    private var current: Record? = null
    private val polyline = MutableLiveData<List<LatLng>>()
    private val ticker = TickerLiveData()

    override fun onCreate() {
        super.onCreate()
        setupNotificationChannel()
        playing.value = false
    }

    override fun isPlaying() = playing.value == true

    override fun initialize(record: Record) {
        current = record
        async { polyline.postValue(locationsDao.getPolyline(record.id).map { LatLng(it.latitude, it.longitude) }) }
    }

    override fun start() {
//        client.setMockMode(true)
        current?.let {
            val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(getString(R.string.service_playing))
                    .setContentText(getString(R.string.service_playing_message))
                    .setSmallIcon(R.drawable.ic_play_arrow_white_24dp)
                    .setContentIntent(pendingIntent)
                    .addAction(R.drawable.ic_clear_white_24dp, getString(R.string.universal_stop), selfStopPendingIntent)
                    .build()
            startForeground(PlayService.NOTIFICATION_ID, notification)
            ticker.start()
            locationHandler.start(it)
            playing.value = true
        }
    }

    override fun stop() {
//        client.setMockMode(false)
        stopForeground(true)
        locationHandler.stop()
        ticker.stop()
        ticker.reset()
        playing.value = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (ACTION_STOP == intent?.action) stop()
        return START_STICKY_COMPATIBILITY
    }

    override fun current(): Record? {
        return current
    }

    override fun playing(): LiveData<Boolean> {
        return playing
    }

    override fun polyline(): LiveData<List<LatLng>> {
        return polyline
    }

    override fun locations(): LiveData<Location> {
        return location
    }

    override fun ticker(): LiveData<Long?> {
        return ticker
    }

    override fun signal(): LiveData<Signal> {
        // FIXME
        return MutableLiveData()
    }

    private fun setupNotificationChannel() {
        val name = getString(R.string.record_new_record)
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, IMPORTANCE_LOW).apply {
            lockscreenVisibility = VISIBILITY_PUBLIC
        }

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        locationHandler.stop()
    }

    override fun onBind(intent: Intent?): IBinder {
        return PlayBinder(this)
    }

    private class PlayBinder(val service: PlayService) : Binder()

    private class LocationHandler(
            db: RecordsDatabase,
            val notify: (recordId: String, location: Location?) -> Unit
    ) {
        private val mainHandler = Handler()
        private val locationsDao = db.locationsDao()
        private val handlerThread = HandlerThread("RecordService")
        private val handler = Handler(handlerThread.apply { start() }.looper)
        private var locationIndex = 0

        private var record: Record? = null
        private var timeDiff: Long? = null

        fun destroy() {
            stop()
        }

        @Synchronized
        fun start(record: Record) {
            this.record = record
            handler.post {
                val next = locationsDao.getByRecordIdAndIndex(record.id, 0)
                emitAndNext(record.id, next)
            }
        }

        @Synchronized
        fun stop() {
            record = null
            timeDiff = null
            locationIndex = 0
            handler.removeCallbacksAndMessages(null)
        }

        @Synchronized
        private fun emitAndNext(recordId: String, stamp: LocationStamp?) {
            if (recordId != record?.id) return // already stopped
            if (stamp == null) {
                stop()
                emit(recordId, stamp)
                return
            }

            if (timeDiff == null) timeDiff = System.currentTimeMillis() - stamp.time

            emit(recordId, stamp)


            handler.post {
                val next = locationsDao.getByRecordIdAndIndex(recordId, stamp.index + 1)
                if (next == null) {
                    emitAndNext(recordId, null)
                    return@post
                }
                val diff = timeDiff ?: return@post
                val time = next.time + diff
                val delay = Math.max(0, time - System.currentTimeMillis())
                handler.postDelayed({
                    emitAndNext(recordId, next)
                }, delay)
            }
        }

        private fun emit(recordId: String, stamp: LocationStamp?) {
            if (stamp == null) {
                postNotify(recordId, null)
                return
            }

            val l = Location("MOCK")
            l.time = System.currentTimeMillis()
            l.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
            l.latitude = stamp.latitude
            l.longitude = stamp.longitude
            stamp.altitude?.let { l.altitude = it }
            stamp.speed?.let { l.speed = it }
            stamp.bearing?.let { l.bearing = it }
            stamp.bearingAccuracyDegrees?.let { l.bearingAccuracyDegrees = it }
            stamp.speedAccuracyMetersPerSecond?.let { l.speedAccuracyMetersPerSecond = it }
            stamp.horizontalAccuracyMeters?.let { l.accuracy = it }
            stamp.verticalAccuracyMeters?.let { l.verticalAccuracyMeters = it }

            postNotify(recordId, l)
        }

        fun postNotify(recordId: String, location: Location?) {
            mainHandler.post { notify(recordId, location) }
        }
    }
}
