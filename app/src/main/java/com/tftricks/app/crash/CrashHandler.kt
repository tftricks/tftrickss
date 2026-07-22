package com.tftricks.app.crash

import android.content.Context
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Global uncaught-exception handler: writes the crash's stack trace to a file before
 * letting the default handler run (the OS crash dialog / process death still happens
 * normally — this only records the trace first), so it can be read back on the Crash Log
 * screen (More tab) instead of guessing at bug reports from a repro description alone.
 */
object CrashHandler {
    private const val TAG = "CrashHandler"
    private const val FILE_NAME = "last_crash.txt"

    fun install(context: Context) {
        val appContext = context.applicationContext
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                writeCrashLog(appContext, thread, throwable)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to write crash log", e)
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun writeCrashLog(context: Context, thread: Thread, throwable: Throwable) {
        val stringWriter = StringWriter()
        throwable.printStackTrace(PrintWriter(stringWriter))
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        val content = buildString {
            appendLine("Crashed at: $timestamp")
            appendLine("Thread: ${thread.name}")
            appendLine()
            append(stringWriter.toString())
        }
        File(context.filesDir, FILE_NAME).writeText(content)
    }

    /** The last recorded crash log's full text, or null if none is stored. */
    fun readLastCrash(context: Context): String? {
        val file = File(context.applicationContext.filesDir, FILE_NAME)
        return if (file.exists()) file.readText() else null
    }

    /** Clears the stored crash log. */
    fun clear(context: Context) {
        File(context.applicationContext.filesDir, FILE_NAME).delete()
    }
}
