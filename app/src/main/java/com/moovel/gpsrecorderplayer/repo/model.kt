package com.moovel.gpsrecorderplayer.repo

import android.os.Parcelable
import android.os.SystemClock
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.android.parcel.Parcelize

@Entity(
        tableName = "records",
        primaryKeys = ["id"],
        indices = [(Index("id")), Index("name")]
)
@Parcelize
data class Record(
        val id: String,
        val name: String,
        val start: Long = System.currentTimeMillis(),
        val created: Long = SystemClock.elapsedRealtimeNanos()
) : Parcelable

@Entity(
        tableName = "locations",
        primaryKeys = ["index", "record_id"],
        indices = [Index("record_id"), Index("record_id", "index")],
        foreignKeys = [(ForeignKey(entity = Record::class, parentColumns = ["id"], childColumns = ["record_id"], onDelete = ForeignKey.CASCADE))]
)
data class LocationStamp(
        @ColumnInfo(name = "record_id")
        val recordId: String,
        val index: Int,
        val created: Long = SystemClock.elapsedRealtime(),

        val provider: String,
        val time: Long,
        val elapsedRealtimeNanos: Long,
        val latitude: Double,
        val longitude: Double,
        val altitude: Double? = null,
        val speed: Float? = null,
        val bearing: Float? = null,
        val horizontalAccuracyMeters: Float? = null,
        val verticalAccuracyMeters: Float? = null,
        val speedAccuracyMetersPerSecond: Float? = null,
        val bearingAccuracyDegrees: Float? = null
)

@Entity(
        tableName = "signals",
        primaryKeys = ["index", "record_id"],
        indices = [Index("record_id"), Index("record_id", "index")],
        foreignKeys = [ForeignKey(entity = Record::class, parentColumns = ["id"], childColumns = ["record_id"], onDelete = ForeignKey.CASCADE)]
)
data class SignalStamp(
        @ColumnInfo(name = "record_id")
        val recordId: String,
        val index: Int,
        val created: Long =  SystemClock.elapsedRealtimeNanos(),

        @ColumnInfo(name = "network_type")
        val networkType: Int,

        @ColumnInfo(name = "service_state")
        val serviceState: Int,

        @ColumnInfo(name = "gsm_signal_strength")
        val gsmSignalStrength: Int,
        @ColumnInfo(name = "gsm_bit_error_rate")
        val gsmBitErrorRate: Int,
        @ColumnInfo(name = "cdma_dbm")
        val cdmaDbm: Int,
        @ColumnInfo(name = "cdma_ecio")
        val cdmaEcio: Int,
        @ColumnInfo(name = "evdo_dbm")
        val evdoDbm: Int,
        @ColumnInfo(name = "evdo_ecio")
        val evdoEcio: Int,
        @ColumnInfo(name = "evdo_snr")
        val evdoSnr: Int,
        val gsm: Boolean = false,
        val level: Int
)

data class Position(
        val latitude: Double,
        val longitude: Double
)