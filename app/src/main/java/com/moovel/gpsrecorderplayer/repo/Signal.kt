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
    companion object {
        const val LEVEL_NONE = 0
        const val LEVEL_POOR = 1
        const val LEVEL_MODERATE = 2
        const val LEVEL_GOOD = 3
        const val LEVEL_GREAT = 4

        fun networkTypeName(networkType: Int) = when (networkType) {
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

        fun networkClassName(networkType: Int) = when (networkType) {
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

        fun serviceStateName(serviceState: Int) = when (serviceState) {
            ServiceState.STATE_IN_SERVICE -> "IN_SERVICE"
            ServiceState.STATE_OUT_OF_SERVICE -> "OUT_OF_SERVICE"
            ServiceState.STATE_EMERGENCY_ONLY -> "EMERGENCY_ONLY"
            ServiceState.STATE_POWER_OFF -> "POWER_OFF"
            else -> "UNSUPPORTED($serviceState)"
        }

        fun levelName(level: Int) = when (level) {
            0 -> "NONE"
            1 -> "POOR"
            2 -> "MODERATE"
            3 -> "GOOD"
            4 -> "GREAT"
            else -> "UNSUPPORTED($level)"
        }
    }

    val networkTypeName: String by lazy { networkTypeName(networkType) }

    val networkClassName: String by lazy { networkClassName(networkType) }

    val levelName: String by lazy { levelName(level) }
}
