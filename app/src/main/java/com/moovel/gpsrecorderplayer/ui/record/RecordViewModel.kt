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

package com.moovel.gpsrecorderplayer.ui.record

import android.app.Application
import android.content.ComponentName
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import android.location.Location
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.moovel.gpsrecorderplayer.repo.Record
import com.moovel.gpsrecorderplayer.service.IRecordService
import com.moovel.gpsrecorderplayer.service.RecordService
import com.moovel.gpsrecorderplayer.repo.Signal
import com.moovel.gpsrecorderplayer.utils.switchMap

class RecordViewModel(application: Application) : AndroidViewModel(application) {

    private val serviceLiveData: MutableLiveData<IRecordService?> = MutableLiveData()

    val locationLiveData: LiveData<Location?> = serviceLiveData.switchMap { it?.locations() }
    val signalLiveData: LiveData<Signal?> = serviceLiveData.switchMap { it?.signal() }
    val recordingLiveData: LiveData<Boolean> = serviceLiveData.switchMap { it?.recording() }
    val tickerLiveData: LiveData<Long?> = serviceLiveData.switchMap { it?.ticker() }
    val polyline: LiveData<List<LatLng>> = serviceLiveData.switchMap { it?.polyline() }

    private var service: IRecordService? = null
    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {
            serviceLiveData.value = null
        }

        override fun onServiceConnected(p0: ComponentName, binder: IBinder) {
            service = RecordService.of(binder)
            serviceLiveData.value = service
        }
    }

    init {
        application.bindService(Intent(application, RecordService::class.java), connection, BIND_AUTO_CREATE)
    }

    override fun onCleared() {
        getApplication<Application>().unbindService(connection)
    }

    fun start(name: String): Record? {
        return service?.start(name)
    }

    fun stop(recordName: String) {
        service?.rename(recordName)
        service?.stop()
    }

    fun isRecording() = service?.isRecording() == true
}
