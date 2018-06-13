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

    fun export(context: Context, records: Collection<Record>, result: (Intent?, Throwable?) -> Unit) {
        val handler = Handler()

        val recordsPath = File(context.filesDir, "records")
        recordsPath.mkdir()

        val db = RecordsDatabase.getInstance(context)
        val locationsDao = db.locationsDao()

        async {
            records
                    .mapNotNull { record ->
                        val file = File(recordsPath, "${record.id}.csv")
                        val locations = locationsDao.getByRecordId(record.id)

                        if (locations.isNotEmpty()) {
                            exportLocations(file, locations)
                            file
                        } else {
                            null
                        }
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

    private fun exportLocations(file: File, locations: Collection<LocationStamp>) {
        val rows = listOf<CsvColumn<LocationStamp>>(
                CsvColumn("index") { it.index },
                CsvColumn("created") { it.created },
                CsvColumn("provider") { it.provider },
                CsvColumn("time") { it.time },
                CsvColumn("latitude") { it.latitude },
                CsvColumn("longitude") { it.longitude },
                CsvColumn("altitude") { it.altitude },
                CsvColumn("speed") { it.speed },
                CsvColumn("horizontalAccuracyMeters") { it.horizontalAccuracyMeters },
                CsvColumn("verticalAccuracyMeters") { it.verticalAccuracyMeters },
                CsvColumn("speedAccuracyMetersPerSecond") { it.speedAccuracyMetersPerSecond },
                CsvColumn("bearingAccuracyDegrees") { it.bearingAccuracyDegrees })

        FileWriter(file).useIt {
            append(rows.joinToString(COMMA) { it.name })

            locations.forEach { location ->
                append(LINE_SEPARATOR)
                append(rows.joinToString(COMMA) { it.project(location) })
            }
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
