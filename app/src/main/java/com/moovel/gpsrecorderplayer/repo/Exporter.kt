package com.moovel.gpsrecorderplayer.repo

import android.content.Context
import android.content.Intent
import android.os.Handler
import androidx.core.content.FileProvider
import java.io.BufferedOutputStream
import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object Exporter {
    private const val COMMA = ","
    private const val LINE_SEPARATOR = "\n"
    private const val TYPE = "application/zip"
    private const val FILE_PROVIDER_AUTHORITY = "com.moovel.gpsrecorderplayer.fileprovider"

    private val RECORD_ROWS = arrayListOf<CsvColumn<Record>>(
            CsvColumn("id") { it.id },
            CsvColumn("name") { it.name.replace(COMMA, "") },
            CsvColumn("start") { it.start })

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

        async {
            records
                    .mapNotNull { export(db, it.id, recordsPath) }
                    .map { FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, it) }
                    .let { ArrayList(it) }
                    .let { uris ->
                        when (uris.size) {
                            0 -> null
                            1 -> Intent(Intent.ACTION_SEND)
                                    .putExtra(Intent.EXTRA_STREAM, uris.first())
                                    .setType(TYPE)
                                    .putExtra(Intent.EXTRA_SUBJECT, records.joinToString { it.name })
                                    .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            else -> Intent(Intent.ACTION_SEND_MULTIPLE)
                                    .putExtra(Intent.EXTRA_STREAM, uris)
                                    .setType(TYPE)
                                    .putExtra(Intent.EXTRA_SUBJECT, records.joinToString { it.name })
                                    .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                    }
                    .let {
                        handler.post { result(it, null) }
                    }
        }
    }

    private fun <T> OutputStream.write(columns: Iterable<CsvColumn<T>>, values: Iterable<T>) {
        write(columns.joinToString(COMMA) { it.name }.toByteArray())
        values.forEach { value ->
            write(LINE_SEPARATOR.toByteArray())
            write(columns.joinToString(COMMA) { it.project(value) }.toByteArray())
        }
    }

    private fun export(db: RecordsDatabase, recordId: String, path: File): File? {
        val record = db.recordsDao().getById(recordId) ?: return null
        val locations = db.locationsDao().getByRecordId(recordId)
        val signals = db.signalsDao().getByRecordId(recordId)

        val name = record.name.replace(Regex("([^a-zA-Z0-9])+"), "_")
                .takeIf { it.isNotBlank() } ?: record.id

        val file = File(path, "$name.zip")

        ZipOutputStream(BufferedOutputStream(FileOutputStream(file))).useIt {
            putNextEntry(ZipEntry("record.csv"))
            write(RECORD_ROWS, listOf(record))

            putNextEntry(ZipEntry("locations.csv"))
            write(LOCATION_ROWS, locations)


            putNextEntry(ZipEntry("signals.csv"))
            write(SIGNAL_ROWS, signals)
        }

        return file
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
