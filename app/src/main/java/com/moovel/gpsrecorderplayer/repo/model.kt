package com.moovel.gpsrecorderplayer.repo

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Index
import android.location.Location
import android.os.Parcelable
import android.telephony.SignalStrength
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
        tableName = "positions",
        primaryKeys = ["index", "record_id"],
        indices = [Index("record_id"), Index("record_id", "index")],
        foreignKeys = [ForeignKey(entity = Record::class, parentColumns = ["id"], childColumns = ["record_id"], onDelete = ForeignKey.CASCADE)]
)
data class Position(
        @ColumnInfo(name = "record_id")
        val recordId: String,
        val index: Long,
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

private fun Position.toLocation(): Location {
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
//
//@Entity(
//        tableName = "signals",
//        primaryKeys = ["index", "record_id"],
//        foreignKeys = [ForeignKey(entity = Record::class, parentColumns = ["id"], childColumns = ["record_id"], onDelete = ForeignKey.CASCADE)]
//)
//data class Signal(
//        val index: Int,
//        val recordId: String,
//        val created: Long = System.currentTimeMillis(),
//        val networkType: Int,
//)
