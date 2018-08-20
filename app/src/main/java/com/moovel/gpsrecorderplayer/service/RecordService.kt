/**
 * Copyright (c) 2010-2018 Moovel Group GmbH - moovel.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.moovel.gpsrecorderplayer.service

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
import androidx.lifecycle.Observer
import com.google.android.gms.maps.model.LatLng
import com.moovel.gpsrecorderplayer.R
import com.moovel.gpsrecorderplayer.repo.LocationStamp
import com.moovel.gpsrecorderplayer.repo.Record
import com.moovel.gpsrecorderplayer.repo.RecordsDatabase
import com.moovel.gpsrecorderplayer.repo.Signal
import com.moovel.gpsrecorderplayer.repo.SignalStamp
import com.moovel.gpsrecorderplayer.ui.MainActivity
import java.util.UUID

class RecordService : Service(), IRecordService {
    companion object {
        private const val NOTIFICATION_ID = 0x4554
        private const val NOTIFICATION_CHANNEL_ID = "record"
        private const val ACTION_STOP = "stop"

        fun of(binder: IBinder): IRecordService {
            return (binder as RecordBinder).service
        }

        private fun Location.toStamp(recordId: String, index: Int, created: Long = SystemClock.elapsedRealtimeNanos()) =
                LocationStamp(recordId,
                        index,
                        created,
                        provider,
                        time,
                        elapsedRealtimeNanos,
                        latitude,
                        longitude,
                        if (hasAltitude()) altitude else null,
                        if (hasSpeed()) speed else null,
                        if (hasBearing()) bearing else null,
                        if (hasAccuracy()) accuracy else null,
                        if (hasVerticalAccuracy()) verticalAccuracyMeters else null,
                        if (hasSpeedAccuracy()) speedAccuracyMetersPerSecond else null,
                        if (hasBearingAccuracy()) bearingAccuracyDegrees else null
                )

        private fun Signal.toStamp(recordId: String, index: Int, created: Long = SystemClock.elapsedRealtimeNanos()) =
                SignalStamp(recordId,
                        index,
                        created,
                        networkType,
                        serviceState,
                        gsmSignalStrength,
                        gsmBitErrorRate,
                        cdmaDbm,
                        cdmaEcio,
                        evdoDbm,
                        evdoEcio,
                        evdoSnr,
                        gsm,
                        level
                )
    }

    private val startActivityIntent by lazy {
        val intent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        PendingIntent.getActivity(this, 0, intent, 0)
    }
    private val stopServiceIntent by lazy {
        val stopSelfIntent = Intent(this, RecordService::class.java).apply { action = ACTION_STOP }
        PendingIntent.getService(this, 0, stopSelfIntent, FLAG_CANCEL_CURRENT)
    }

    private val db by lazy { RecordsDatabase.getInstance(applicationContext) }
    private val recordsDao by lazy { db.recordsDao() }
    private val locationsDao by lazy { db.locationsDao() }
    private val signalsDao by lazy { db.signalsDao() }

    private val handlerThread = HandlerThread("RecordService")
    private val handler = Handler(handlerThread.apply { start() }.looper)
    private val mainHandler = Handler()
    private val ticker = TickerLiveData()

    private var record: Record? = null
    private var locationIndex = 0
    private var signalIndex = 0
    private val signal by lazy { SignalLiveData(this) }
    private val location by lazy { LocationLiveData(this) }
    private val recording = MutableLiveData<Boolean>()
    private val polyline = MutableLiveData<List<LatLng>>()
    private var polylineList = emptyList<LatLng>()
        set(value) {
            field = value
            polyline.value = value
        }

    private val locationObserver = Observer<Location?> {
        it?.let {
            polylineList = polylineList.plus(LatLng(it.latitude, it.longitude))
            record?.insertLocationAsync(locationIndex++, it)
        }
    }

    private val signalObserver = Observer<Signal?> {
        it?.let {
            record?.insertSignalAsync(signalIndex++, it)
        }
    }

    override fun onCreate() {
        super.onCreate()
        setupNotificationChannel()
        recording.value = false
    }

    override fun isRecording() = recording.value == true

    override fun start(name: String): Record {
        if (record != null) throw IllegalStateException("Stop recording before")
        startService(Intent(this, RecordService::class.java))
        val record = Record(UUID.randomUUID().toString(), name)
        this.record = record
        record.insertRecordAsync()
        location.observeForever(locationObserver)
        signal.observeForever(signalObserver)
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(getString(R.string.service_recording))
                .setContentText(getString(R.string.service_recording_message))
                .setSmallIcon(R.drawable.ic_fiber_manual_record_white_24dp)
                .setContentIntent(startActivityIntent)
                .addAction(R.drawable.ic_clear_white_24dp, getString(R.string.universal_stop), stopServiceIntent)
                .build()
        startForeground(NOTIFICATION_ID, notification)
        recording.value = true
        ticker.start()
        return record
    }

    override fun stop(): Record? {
        location.removeObserver(locationObserver)
        signal.removeObserver(signalObserver)
        polylineList = emptyList()
        record?.complete()
        val current = record
        record = null
        locationIndex = 0
        signalIndex = 0
        recording.value = false
        ticker.stop()
        ticker.reset()
        stopSelf()
        return current
    }

    override fun current(): Record? {
        return record
    }

    override fun rename(name: String) {
        if (record == null) throw IllegalStateException("Not recording")
        record = record?.copy(name = name)
        record?.updateRecordAsync()
    }

    override fun ticker(): LiveData<Long?> {
        return ticker
    }

    override fun polyline(): LiveData<List<LatLng>> {
        return polyline
    }

    override fun locations(): LiveData<Location?> {
        return location
    }

    override fun signal(): LiveData<Signal?> {
        return signal
    }

    override fun recording(): LiveData<Boolean> {
        return recording
    }

    private fun Record.insertRecordAsync() {
        handler.post { recordsDao.insert(this) }
    }

    private fun Record.updateRecordAsync() {
        handler.post { recordsDao.update(this) }
    }

    private fun Record.insertLocationAsync(index: Int, location: Location) {
        val created = SystemClock.elapsedRealtime()
        handler.post { locationsDao.insert(location.toStamp(this.id, index, created)) }
    }

    private fun Record.insertSignalAsync(index: Int, signal: Signal) {
        val created = SystemClock.elapsedRealtime()
        handler.post { signalsDao.insert(signal.toStamp(this.id, index, created)) }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (ACTION_STOP == intent?.action) stop()
        return START_STICKY_COMPATIBILITY
    }

    private fun Record.complete() {
        handler.post {
            mainHandler.post {
                if (record?.id == null) {
                    stopForeground(true)
                }
            }
        }
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
        handlerThread.quitSafely()
    }

    override fun onBind(intent: Intent?): IBinder {
        return RecordBinder(this)
    }

    private class RecordBinder(val service: RecordService) : Binder()
}
