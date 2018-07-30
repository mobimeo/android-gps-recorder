package com.moovel.gpsrecorderplayer.repo

import android.annotation.SuppressLint
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
                publish(location)
            }
        }
    }

    private val signalHandler by lazy {
        SignalHandler(db) { recordId: String, signal: Signal? ->
            if (recordId == current?.id) {
                publish(signal)
            }
        }
    }

    private val startActivityIntent by lazy {
        val intent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        PendingIntent.getActivity(this, 0, intent, 0)
    }
    private val stopServiceIntent by lazy {
        val stopSelfIntent = Intent(this, PlayService::class.java).apply { action = ACTION_STOP }
        PendingIntent.getService(this, 0, stopSelfIntent, FLAG_CANCEL_CURRENT)
    }

    private val locationsDao by lazy { db.locationsDao() }
    private val location = MutableLiveData<Location>()
    private val signal = MutableLiveData<Signal>()
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

    @SuppressLint("MissingPermission")
    override fun start() {
        client.setMockMode(true)
        current?.let {
            val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(getString(R.string.service_playing))
                    .setContentText(getString(R.string.service_playing_message))
                    .setSmallIcon(R.drawable.ic_play_arrow_white_24dp)
                    .setContentIntent(startActivityIntent)
                    .addAction(R.drawable.ic_clear_white_24dp, getString(R.string.universal_stop), stopServiceIntent)
                    .build()
            startForeground(PlayService.NOTIFICATION_ID, notification)
            ticker.start()
            locationHandler.start(it)
            signalHandler.start(it)
            playing.value = true
        }
    }

    @SuppressLint("MissingPermission")
    override fun stop() {
        client.setMockMode(false)
        stopForeground(true)
        locationHandler.stop()
        signalHandler.stop()
        ticker.stop()
        ticker.reset()
        playing.value = false
    }

    @SuppressLint("MissingPermission")
    private fun publish(location: Location?) {
        if (location != null) {
            client.setMockLocation(location)
            this.location.value = location
        } else {
            stopWhenHandlerStopped()
        }
    }

    private fun publish(signal: Signal?) {
        if (signal != null) {
            this.signal.value = signal
        } else {
            stopWhenHandlerStopped()
        }
    }

    private fun stopWhenHandlerStopped() {
        if (locationHandler.recordId == null && signalHandler.recordId == null) stop()
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
        return signal
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
        stop()
    }

    override fun onBind(intent: Intent?): IBinder {
        return PlayBinder(this)
    }

    private class PlayBinder(val service: PlayService) : Binder()

    private abstract class RecordStampHandler<S : RecordStamp> {
        private val handlerThread = HandlerThread("${javaClass.simpleName}Thread")
        private val handler = Handler(handlerThread.apply { start() }.looper)
        private var index = 0

        var recordId: String? = null
            private set
        private var started: Long = SystemClock.elapsedRealtime()

        protected abstract fun get(recordId: String, index: Int): S?

        @Synchronized
        fun start(record: Record) {
            recordId = record.id
            started = SystemClock.elapsedRealtime()
            handler.post {
                initAndSchedule(record)
            }
        }

        @Synchronized
        fun stop() {
            recordId = null
            index = 0
            handler.removeCallbacksAndMessages(null)
        }

        private fun scheduleEmit(created: Long, stamp: S, delay: Long) {
            handler.postDelayed({
                emitAndScheduleNext(created, stamp)
            }, delay)
        }

        @Synchronized
        private fun initAndSchedule(record: Record) {
            if (recordId == record.id) {
                val next = get(record.id, 0)
                if (next == null) {
                    emit(record.id, null)
                    stop()
                } else {
                    scheduleEmit(record.created, next, delay(record.created, next))
                }
            }
        }

        @Synchronized
        private fun emitAndScheduleNext(created: Long, stamp: S) {
            if (recordId == stamp.recordId) {
                emit(stamp.recordId, stamp)
                val next = get(stamp.recordId, stamp.index + 1)
                if (next == null) {
                    emit(stamp.recordId, null)
                    stop()
                } else {
                    scheduleEmit(created, next, delay(created, next))
                }
            }
        }

        private fun delay(created: Long, stamp: S): Long {
            val diff = stamp.created - created
            val elapsed = SystemClock.elapsedRealtime() - started
            return diff - elapsed
        }

        protected abstract fun emit(recordId: String, stamp: S?)
    }

    private class LocationHandler(
            db: RecordsDatabase,
            val notify: (recordId: String, location: Location?) -> Unit
    ) : RecordStampHandler<LocationStamp>() {
        private val mainHandler = Handler()
        private val locationsDao = db.locationsDao()

        override fun get(recordId: String, index: Int): LocationStamp? =
                locationsDao.getByRecordIdAndIndex(recordId, index)

        override fun emit(recordId: String, stamp: LocationStamp?) {
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

    private class SignalHandler(
            db: RecordsDatabase,
            val notify: (recordId: String, signal: Signal?) -> Unit
    ) : RecordStampHandler<SignalStamp>() {
        private val mainHandler = Handler()
        private val signalsDao = db.signalsDao()

        override fun get(recordId: String, index: Int): SignalStamp? =
                signalsDao.getByRecordIdAndIndex(recordId, index)

        override fun emit(recordId: String, stamp: SignalStamp?) {
            if (stamp == null) {
                postNotify(recordId, null)
                return
            }

            val signal = Signal(stamp.networkType,
                    stamp.serviceState,
                    stamp.gsmSignalStrength,
                    stamp.gsmBitErrorRate,
                    stamp.cdmaDbm,
                    stamp.cdmaEcio,
                    stamp.evdoDbm,
                    stamp.evdoEcio,
                    stamp.evdoSnr,
                    stamp.gsm,
                    stamp.level)

            postNotify(recordId, signal)
        }

        fun postNotify(recordId: String, signal: Signal?) {
            mainHandler.post { notify(recordId, signal) }
        }
    }
}
