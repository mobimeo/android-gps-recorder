package com.moovel.gpsrecorderplayer.repo

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.moovel.gpsrecorderplayer.R

class PlayService : Service(), IPlayService {
    companion object {
        private const val NOTIFICATION_ID = 0x4654
        private const val NOTIFICATION_CHANNEL_ID = "play"

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

    private val location = MutableLiveData<Location>()
    private val playing = MutableLiveData<Boolean>()
    private var current: Record? = null

    override fun onCreate() {
        super.onCreate()
        setupNotificationChannel()
        playing.value = false
    }

    override fun start(record: Record) {
        current = record
//        client.setMockMode(true)
        locationHandler.start(record)
        playing.value = true
    }

    override fun stop() {
//        client.setMockMode(false)
        locationHandler.stop()
        current = null
        playing.value = false
    }

    override fun current(): Record? {
        return current
    }

    override fun isPlaying(): LiveData<Boolean> {
        return playing
    }

    override fun polyline(): LiveData<List<LatLng>> {
        // FIXME
        return MutableLiveData()
    }

    override fun locations(): LiveData<Location> {
        return location
    }

    override fun signal(): LiveData<Signal> {
        // FIXME
        return MutableLiveData()
    }

    private fun setupNotificationChannel() {
        val name = getString(R.string.record_new_record)
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT)

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
            l.elapsedRealtimeNanos = System.nanoTime()
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
