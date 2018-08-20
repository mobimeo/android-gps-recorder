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

import android.content.Context
import android.telephony.PhoneStateListener
import android.telephony.ServiceState
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import androidx.lifecycle.LiveData
import com.moovel.gpsrecorderplayer.repo.Signal

internal class SignalLiveData(context: Context) : LiveData<Signal?>() {

    companion object {
        private const val EVENTS = PhoneStateListener.LISTEN_SIGNAL_STRENGTHS or
                PhoneStateListener.LISTEN_DATA_CONNECTION_STATE or
                PhoneStateListener.LISTEN_SERVICE_STATE
    }

    private val telephonyManager =
            context.applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    private var networkType: Int? = null
    private var serviceState: ServiceState? = null
    private var signalStrength: SignalStrength? = null

    private val listener: PhoneStateListener = object : PhoneStateListener() {

        override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
            this@SignalLiveData.signalStrength = signalStrength
            onChanged()
        }

        override fun onDataConnectionStateChanged(state: Int, networkType: Int) {
            this@SignalLiveData.networkType = networkType
            onChanged()
        }

        override fun onServiceStateChanged(serviceState: ServiceState) {
            this@SignalLiveData.serviceState = serviceState
            onChanged()
        }
    }

    private fun onChanged() {
        val type = networkType ?: return
        val state = serviceState ?: return
        val strength = signalStrength ?: return

        value = Signal(type,
                state.state,
                strength.gsmSignalStrength,
                strength.gsmBitErrorRate,
                strength.cdmaDbm,
                strength.cdmaEcio,
                strength.evdoDbm,
                strength.evdoEcio,
                strength.evdoSnr,
                strength.isGsm,
                strength.level)
    }

    override fun onActive() {
        telephonyManager.listen(listener, EVENTS)
    }

    override fun onInactive() {
        networkType = null
        serviceState = null
        signalStrength = null
        telephonyManager.listen(listener, PhoneStateListener.LISTEN_NONE)
    }
}
