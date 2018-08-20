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

package com.moovel.gpsrecorderplayer.ui.playback

import android.app.Application
import android.content.ComponentName
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.moovel.gpsrecorderplayer.repo.Record
import com.moovel.gpsrecorderplayer.repo.RecordsDatabase
import com.moovel.gpsrecorderplayer.serialization.Exporter
import com.moovel.gpsrecorderplayer.service.IPlayService
import com.moovel.gpsrecorderplayer.service.PlayService
import com.moovel.gpsrecorderplayer.utils.async
import com.moovel.gpsrecorderplayer.utils.switchMap

class PlayViewModel(application: Application) : AndroidViewModel(application) {

    private val service: MutableLiveData<IPlayService?> = MutableLiveData()

    private var pendingRecord: Record? = null

    val location = service.switchMap { it?.locations() }
    val signal = service.switchMap { it?.signal() }
    val playing: LiveData<Boolean> = service.switchMap { it?.playing() }
    val tickerLiveData: LiveData<Long?> = service.switchMap { it?.ticker() }
    val polyline: LiveData<List<LatLng>?> = service.switchMap { it?.polyline() }

    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            service.value = null
        }

        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            val srv = PlayService.of(binder)
            service.value = srv
            val record = pendingRecord
            pendingRecord = null
            record?.let { srv.initialize(it) }
        }
    }

    init {
        application.bindService(Intent(application, PlayService::class.java), connection, BIND_AUTO_CREATE)
    }

    override fun onCleared() {
        getApplication<Application>().unbindService(connection)
    }

    fun initialize(record: Record) {
        val srv = service.value
        if (srv != null) {
            srv.initialize(record)
        } else {
            pendingRecord = record
        }
    }

    fun play() {
        service.value?.start()
    }

    fun stop() {
        service.value?.stop()
    }

    fun export(record: Record, result: (Intent?, Throwable?) -> Unit) {
        Exporter.export(getApplication(), listOf(record), result)
    }

    fun remove(record: Record) {
        async { RecordsDatabase.getInstance(getApplication()).recordsDao().delete(record) }
    }
}
