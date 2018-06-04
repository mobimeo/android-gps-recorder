package com.moovel.gpsrecorderplayer.repo

import android.arch.lifecycle.LiveData
import android.content.Context
import android.telephony.PhoneStateListener
import android.telephony.ServiceState
import android.telephony.SignalStrength
import android.telephony.TelephonyManager

internal class SignalLiveData(context: Context) : LiveData<Signal>() {

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