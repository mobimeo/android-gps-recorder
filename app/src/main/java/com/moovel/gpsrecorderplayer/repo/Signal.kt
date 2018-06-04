package com.moovel.gpsrecorderplayer.repo

import android.telephony.ServiceState
import android.telephony.TelephonyManager

data class Signal(
        /**
         * see [android.telephony.TelephonyManager.getNetworkType]
         */
        val networkType: Int,

        /**
         * see [android.telephony.TelephonyManager.getServiceState]
         */
        val serviceState: Int,

        /**
         * see [android.telephony.SignalStrength]
         */
        val gsmSignalStrength: Int,
        val gsmBitErrorRate: Int,
        val cdmaDbm: Int,
        val cdmaEcio: Int,
        val evdoDbm: Int,
        val evdoEcio: Int,
        val evdoSnr: Int,
        val gsm: Boolean = false,
        val level: Int
) {
    val networkTypeName: String = when (networkType) {
        TelephonyManager.NETWORK_TYPE_UNKNOWN -> "UNKNOWN"
        TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS"
        TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE"
        TelephonyManager.NETWORK_TYPE_UMTS -> "UMTS"
        TelephonyManager.NETWORK_TYPE_HSDPA -> "HSDPA"
        TelephonyManager.NETWORK_TYPE_HSUPA -> "HSUPA"
        TelephonyManager.NETWORK_TYPE_HSPA -> "HSPA"
        TelephonyManager.NETWORK_TYPE_CDMA -> "CDMA"
        TelephonyManager.NETWORK_TYPE_EVDO_0 -> "CDMA - EvDo rev. 0"
        TelephonyManager.NETWORK_TYPE_EVDO_A -> "CDMA - EvDo rev. A"
        TelephonyManager.NETWORK_TYPE_EVDO_B -> "CDMA - EvDo rev. B"
        TelephonyManager.NETWORK_TYPE_1xRTT -> "CDMA - 1xRTT"
        TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
        TelephonyManager.NETWORK_TYPE_EHRPD -> "CDMA - eHRPD"
        TelephonyManager.NETWORK_TYPE_IDEN -> "iDEN"
        TelephonyManager.NETWORK_TYPE_HSPAP -> "HSPA+"
        TelephonyManager.NETWORK_TYPE_GSM -> "GSM"
        TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "TD_SCDMA"
        TelephonyManager.NETWORK_TYPE_IWLAN -> "IWLAN"
        else -> "UNSUPPORTED($networkType)"
    }

    val networkClassName: String = when (networkType) {
        TelephonyManager.NETWORK_TYPE_UNKNOWN -> "UNKNOWN"
        TelephonyManager.NETWORK_TYPE_GPRS -> "2G"
        TelephonyManager.NETWORK_TYPE_EDGE -> "2G"
        TelephonyManager.NETWORK_TYPE_CDMA -> "2G"
        TelephonyManager.NETWORK_TYPE_1xRTT -> "2G"
        TelephonyManager.NETWORK_TYPE_IDEN -> "2G"
        TelephonyManager.NETWORK_TYPE_GSM -> "2G"
        TelephonyManager.NETWORK_TYPE_UMTS -> "3G"
        TelephonyManager.NETWORK_TYPE_EVDO_0 -> "3G"
        TelephonyManager.NETWORK_TYPE_EVDO_A -> "3G"
        TelephonyManager.NETWORK_TYPE_HSDPA -> "3G"
        TelephonyManager.NETWORK_TYPE_HSUPA -> "3G"
        TelephonyManager.NETWORK_TYPE_HSPA -> "3G"
        TelephonyManager.NETWORK_TYPE_EVDO_B -> "3G"
        TelephonyManager.NETWORK_TYPE_EHRPD -> "3G"
        TelephonyManager.NETWORK_TYPE_HSPAP -> "3G"
        TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "3G"
        TelephonyManager.NETWORK_TYPE_LTE -> "4G"
        TelephonyManager.NETWORK_TYPE_IWLAN -> "4G"
        else -> "UNSUPPORTED($networkType)"
    }

    val serviceStateName: String = when (serviceState) {
        ServiceState.STATE_IN_SERVICE -> "IN_SERVICE"
        ServiceState.STATE_OUT_OF_SERVICE -> "OUT_OF_SERVICE"
        ServiceState.STATE_EMERGENCY_ONLY -> "EMERGENCY_ONLY"
        ServiceState.STATE_POWER_OFF -> "POWER_OFF"
        else -> "UNSUPPORTED($serviceState)"
    }

    val levelName: String = when (level) {
        0 -> "NONE"
        1 -> "POOR"
        2 -> "MODERATE"
        3 -> "GOOD"
        4 -> "GREAT"
        else -> "UNSUPPORTED($level)"
    }
}