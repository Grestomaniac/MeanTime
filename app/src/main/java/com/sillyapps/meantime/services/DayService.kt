package com.sillyapps.meantime.services

import android.app.*
import android.content.Intent
import android.os.IBinder
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.databinding.Observable
import com.hypertrack.hyperlog.HyperLog
import com.sillyapps.meantime.*
import com.sillyapps.meantime.ui.alarmscreen.AlarmActivity
import com.sillyapps.meantime.ui.mainscreen.DayManager
import com.sillyapps.meantime.utils.FileLogger
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.log

@AndroidEntryPoint
class DayService: Service() {
    companion object {
        val SERVICE_ID = 1
        val ACTION_STOP = "STOP"
        val ACTION_START = "START"
    }

    @Inject lateinit var dayManager: DayManager
    private lateinit var intentToActivity: PendingIntent

    private lateinit var notificationBuilder: NotificationCompat.Builder

    private lateinit var pauseIntent: PendingIntent
    private lateinit var stopIntent: PendingIntent
    private lateinit var notificationManager: NotificationManager


    override fun onCreate() {
        super.onCreate()
        FileLogger.d("Service created")

        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        setupObservers()

        setupButtonsIntents()
        setupNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        FileLogger.d("Service onStartCommand")
        if (intent != null) {
            when (intent.action) {
                ACTION_STOP -> {
                    dayManager.getNextTask(true)
                    updateTimer()
                    FileLogger.d("Service is stopped")
                }
                ACTION_START -> {
                    FileLogger.d("MainActivity requests for service")
                }
                else -> {
                    FileLogger.d("Unknown intent")
                }
            }
        }
        else {
            FileLogger.d("with a null intent. It has been probably restarted by the system")
        }

        return START_STICKY
    }

    private fun setupObservers() {
        // Observe changes on day, if change update notification
        dayManager.thisDay!!.addOnPropertyChangedCallback(notificationUpdateCallback)
    }

    private fun setupNotification() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        intentToActivity = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        setNotificationBuilder()
        startForeground(SERVICE_ID, notificationBuilder.build())
    }

    private fun setupButtonsIntents() {
        stopIntent = Intent(this, DayService::class.java).let {
            it.action = ACTION_STOP
            PendingIntent.getService(this, 0, it, 0)
        }
    }

    private fun setNotificationBuilder() {
        val currentDay = dayManager.thisDay!!
        val timeRemain = convertMillisToStringFormatWithSeconds(currentDay.timeRemain)

        notificationBuilder =  NotificationCompat.Builder(this, AppConstants.SERVICE_MAIN_NOTIFICATION_CHANNEL)
            .setContentTitle(currentDay.getCurrentTask().name)
            .setContentText(timeRemain)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(intentToActivity)
            .setAutoCancel(false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(R.drawable.ic_stop_black_24dp, getString(R.string.stop), stopIntent)
    }

    private val notificationUpdateCallback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            when (propertyId) {
                BR.timeRemain -> {
                    updateTimer()

                }

                AppBR.taskFinishedNaturally -> {
                    turnOnAlarm()

                }

                BR.runningState -> {
                    stopService()
                }
            }
        }
    }

    private fun updateTimer() {
        val currentDay = dayManager.thisDay!!
        val timeRemain = convertMillisToStringFormatWithSeconds(currentDay.timeRemain)
        notificationBuilder.setContentText(timeRemain)
        notificationBuilder.setContentTitle(currentDay.getCurrentTask().name)

        notificationManager.notify(SERVICE_ID, notificationBuilder.build())
    }

    private fun stopService() {
        if (!dayManager.thisDay!!.runningState)
            stopSelf()
    }

    private fun turnOnAlarm() {
        val alarmIntent = Intent(this, AlarmActivity::class.java)
        alarmIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK + Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(alarmIntent)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        FileLogger.d("Service is destroyed")
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        FileLogger.d("Service on task removed")
    }

    override fun onLowMemory() {
        super.onLowMemory()
        FileLogger.d("System on low memory")
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        FileLogger.d("System trimming memory")
    }
}