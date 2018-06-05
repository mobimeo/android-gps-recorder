package com.moovel.gpsrecorderplayer.repo

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Index
import android.location.Location
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Entity(
        tableName = "records",
        primaryKeys = ["id"],
        indices = [Index("id")]
)

@Parcelize
data class Record(
        val id: String,
        val name: String,
        val start: Long = System.currentTimeMillis()
) : Parcelable

@Entity(
        tableName = "locations",
        primaryKeys = ["index", "record_id"],
        indices = [Index("record_id"), Index("record_id", "index")],
        foreignKeys = [ForeignKey(entity = Record::class, parentColumns = ["id"], childColumns = ["record_id"], onDelete = ForeignKey.CASCADE)]
)
data class LocationStamp(
        @ColumnInfo(name = "record_id")
        val recordId: String,
        val index: Int,
        val created: Long = System.currentTimeMillis(),

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

private fun LocationStamp.toLocation(): Location {
    val l = Location(provider)
    l.time = time
    l.elapsedRealtimeNanos = elapsedRealtimeNanos
    l.latitude = latitude
    l.longitude = longitude
    altitude?.let { l.altitude = it }
    speed?.let { l.speed = it }
    bearing?.let { l.bearing = it }
    bearingAccuracyDegrees?.let { l.bearingAccuracyDegrees = it }
    speedAccuracyMetersPerSecond?.let { l.speedAccuracyMetersPerSecond = it }
    horizontalAccuracyMeters?.let { l.accuracy = it }
    verticalAccuracyMeters?.let { l.verticalAccuracyMeters = it }
    return l
}

@Entity(
        tableName = "signals",
        primaryKeys = ["index", "record_id"],
        foreignKeys = [ForeignKey(entity = Record::class, parentColumns = ["id"], childColumns = ["record_id"], onDelete = ForeignKey.CASCADE)]
)
data class SignalStamp(
        @ColumnInfo(name = "record_id")
        val recordId: String,
        val index: Int,
        val created: Long = System.currentTimeMillis(),

        val networkType: Int,

        val serviceState: Int,

        val gsmSignalStrength: Int,
        val gsmBitErrorRate: Int,
        val cdmaDbm: Int,
        val cdmaEcio: Int,
        val evdoDbm: Int,
        val evdoEcio: Int,
        val evdoSnr: Int,
        val gsm: Boolean = false,
        val level: Int
)
