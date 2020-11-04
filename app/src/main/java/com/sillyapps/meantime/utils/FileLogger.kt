package com.sillyapps.meantime.utils

import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object FileLogger {

    private val TAG = "MeanTime"
    private val NEW_LINE = System.getProperty("line.separator")
    var mLogcatAppender = true
    var mLogFile: File? = null

    fun initialize(context: Context, clearFile: Boolean = false) {
        mLogFile = File(context.filesDir, "logs.txt")

        if (clearFile) mLogFile?.delete()

        if (!mLogFile!!.exists()) {
            try {
                mLogFile?.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        logDeviceInfo()
    }

    fun i(message: String) {
        appendLog("$TAG : $message")
        if (mLogcatAppender) {
            Log.i(TAG, message)
        }
    }

    fun d(message: String) {
        appendLog("$TAG : $message")
        if (mLogcatAppender) {
            Log.d(TAG, message)
        }
    }

    fun e(message: String) {
        appendLog("$TAG : $message")
        if (mLogcatAppender) {
            Log.e(TAG, message)
        }
    }

    fun v(message: String) {
        appendLog("$TAG : $message")
        if (mLogcatAppender) {
            Log.v(TAG, message)
        }
    }

    fun w(message: String) {
        appendLog("$TAG : $message")
        if (mLogcatAppender) {
            Log.w(TAG, message)
        }
    }

    @Synchronized
    private fun appendLog(text: String) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
        try {
            val fileOut = FileWriter(mLogFile, true)
            fileOut.append(sdf.format(Date()).toString() + " : " + text + NEW_LINE)
            fileOut.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun logDeviceInfo() {
        appendLog("Model : " + Build.MODEL)
        appendLog("Brand : " + Build.BRAND)
        appendLog("Product : " + Build.PRODUCT)
        appendLog("Device : " + Build.DEVICE)
        appendLog("Codename : " + Build.VERSION.CODENAME)
        appendLog("Release : " + Build.VERSION.RELEASE)
    }
}