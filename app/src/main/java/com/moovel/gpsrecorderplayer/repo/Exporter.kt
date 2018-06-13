package com.moovel.gpsrecorderplayer.repo

import android.content.Context
import android.content.Intent
import android.os.Handler
import androidx.core.content.FileProvider
import java.io.Closeable
import java.io.File
import java.io.FileWriter

object Exporter {
    private const val COMMA = ","
    private const val LINE_SEPARATOR = "\n"
    private const val TYPE = "test/plain"
    private const val FILE_PROVIDER_AUTHORITY = "com.moovel.gpsrecorderplayer.fileprovider"

    private val LOCATION_ROWS = arrayListOf<CsvColumn<LocationStamp>>(
            CsvColumn("index") { it.index },
            CsvColumn("created") { it.created },
            CsvColumn("provider") { it.provider },
            CsvColumn("time") { it.time },
            CsvColumn("latitude") { it.latitude },
            CsvColumn("longitude") { it.longitude },
            CsvColumn("altitude") { it.altitude },
            CsvColumn("speed") { it.speed },
            CsvColumn("bearing") { it.bearing },
            CsvColumn("horizontalAccuracyMeters") { it.horizontalAccuracyMeters },
            CsvColumn("verticalAccuracyMeters") { it.verticalAccuracyMeters },
            CsvColumn("speedAccuracyMetersPerSecond") { it.speedAccuracyMetersPerSecond },
            CsvColumn("bearingAccuracyDegrees") { it.bearingAccuracyDegrees })

    private val SIGNAL_ROWS = arrayListOf<CsvColumn<SignalStamp>>(
            CsvColumn("index") { it.index },
            CsvColumn("created") { it.created },
            CsvColumn("networkType") { it.networkType },
            CsvColumn("networkTypeName") { Signal.networkTypeName(it.networkType) },
            CsvColumn("networkClassName") { Signal.networkClassName(it.networkType) },
            CsvColumn("serviceState") { it.serviceState },
            CsvColumn("serviceStateName") { Signal.serviceStateName(it.serviceState) },
            CsvColumn("gsmSignalStrength") { it.gsmSignalStrength },
            CsvColumn("gsmBitErrorRate") { it.gsmBitErrorRate },
            CsvColumn("cdmaDbm") { it.cdmaDbm },
            CsvColumn("cdmaEcio") { it.cdmaEcio },
            CsvColumn("evdoDbm") { it.evdoDbm },
            CsvColumn("evdoEcio") { it.evdoEcio },
            CsvColumn("evdoSnr") { it.evdoSnr },
            CsvColumn("gsm") { it.gsm },
            CsvColumn("level") { it.level },
            CsvColumn("levelName") { Signal.levelName(it.level) })

    fun export(context: Context, records: Collection<Record>, result: (Intent?, Throwable?) -> Unit) {
        val handler = Handler()

        val recordsPath = File(context.filesDir, "records").apply { mkdir() }

        val db = RecordsDatabase.getInstance(context)
        val locationsDao = db.locationsDao()
        val signalsDao = db.signalsDao()

        async {
            records
                    .flatMap { record ->
                        val name = record.name.replace(Regex("([^a-zA-Z0-9])+"), "_")
                                .takeIf { it.isNotBlank() } ?: record.id
                        val recordPath = File(recordsPath, record.id).apply { mkdir() }

                        val locationsFile = locationsDao.getByRecordId(record.id)
                                .takeIf { it.isNotEmpty() }
                                ?.let {
                                    val file = File(recordPath, "${name}_locations.csv")
                                    exportLocations(file, it)
                                    file
                                }

                        val signalsFile = signalsDao.getByRecordId(record.id)
                                .takeIf { it.isNotEmpty() }
                                ?.let {
                                    val file = File(recordPath, "${name}_signals.csv")
                                    exportSignals(file, it)
                                    file
                                }

                        listOfNotNull(locationsFile, signalsFile)
                    }
                    .map { FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, it) }
                    .let { ArrayList(it) }
                    .let { uris ->
                        when (uris.size) {
                            0 -> null
                            1 -> Intent(Intent.ACTION_SEND)
                                    .putExtra(Intent.EXTRA_STREAM, uris.first())
                                    .setType(TYPE)
                                    .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            else -> Intent(Intent.ACTION_SEND_MULTIPLE)
                                    .putExtra(Intent.EXTRA_STREAM, uris)
                                    .setType(TYPE)
                                    .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                    }
                    .let {
                        handler.post { result(it, null) }
                    }
        }
    }

    private fun exportLocations(file: File, locations: Collection<LocationStamp>) = FileWriter(file).useIt {
        append(LOCATION_ROWS.joinToString(COMMA) { it.name })

        locations.forEach { location ->
            append(LINE_SEPARATOR)
            append(LOCATION_ROWS.joinToString(COMMA) { it.project(location) })
        }
    }

    private fun exportSignals(file: File, signals: Collection<SignalStamp>) = FileWriter(file).useIt {
        append(SIGNAL_ROWS.joinToString(COMMA) { it.name })

        signals.forEach { location ->
            append(LINE_SEPARATOR)
            append(SIGNAL_ROWS.joinToString(COMMA) { it.project(location) })
        }
    }

    private fun <T : Closeable, R> T.useIt(block: T.() -> R): R {
        return use { it.block() }
    }

    private class CsvColumn<T>(val name: String, private val projection: (T) -> Any?) {
        fun project(value: T): String {
            return projection(value).toString()
        }
    }
}
