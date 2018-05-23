package com.moovel.gpsrecorderplayer.repo

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey
import android.location.Location
import java.time.ZonedDateTime

@Entity(tableName = "records")
data class Record(
        @PrimaryKey
        val id: String,
        val name: String
//        val start: ZonedDateTime = ZonedDateTime.now(),
//        val end: ZonedDateTime? = null
)

@Entity(
        tableName = "locations",
        primaryKeys = ["index", "record_id"],
        foreignKeys = [ForeignKey(entity = Record::class, parentColumns = ["id"], childColumns = ["record_id"], onDelete = ForeignKey.CASCADE)]
)
data class LocationEntity(
        @ColumnInfo(name = "record_id")
        val recordId: String,
        val index: Long,
//        val created: ZonedDateTime,

        val provider: String,
        val time: Long,
        val elapsedRealtimeNanos: Long,
        val latitude: Double,
        val longitude: Double,
        val altitude: Double?,
        val speed: Float?,
        val bearing: Float?,
        val horizontalAccuracyMeters: Float?,
        val verticalAccuracyMeters: Float?,
        val speedAccuracyMetersPerSecond: Float?,
        val bearingAccuracyDegrees: Float?
)

fun Location.toLocationEntity(
        recordId: String,
        index: Long,
        created: ZonedDateTime = ZonedDateTime.now()
): LocationEntity {
    return LocationEntity(recordId, index,
//            created,
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
}

fun LocationEntity.toLocation(): Location {
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

//@Entity(
//        tableName = "signals",
//        foreignKeys = [ForeignKey(entity = Record::class, parentColumns = ["id"], childColumns = ["record_id"], onDelete = ForeignKey.CASCADE)]
//)
//data class SignalEntity(
//        val recordId: String
//)
