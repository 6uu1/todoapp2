package com.todo.mygo.timer.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.todo.mygo.MainActivity
import com.todo.mygo.R
import com.todo.mygo.timer.data.PomodoroSession
import com.todo.mygo.timer.data.PomodoroStatus
import com.todo.mygo.timer.data.TimerRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * 计时器服务，用于在后台运行计时器
 */
class TimerService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "timer_channel"
        private const val CHANNEL_NAME = "Timer Notifications"

        // 广播 Action
        const val ACTION_TIMER_TICK = "com.todo.mygo.timer.ACTION_TIMER_TICK"
        const val ACTION_TIMER_FINISH = "com.todo.mygo.timer.ACTION_TIMER_FINISH"
        const val ACTION_POMODORO_STATE_CHANGED = "com.todo.mygo.timer.ACTION_POMODORO_STATE_CHANGED"

        // 广播 Extra
        const val EXTRA_TIME_REMAINING = "time_remaining"
        const val EXTRA_POMODORO_STATUS = "pomodoro_status"
        const val EXTRA_TIMER_TYPE = "timer_type"
        const val EXTRA_ELAPSED_TIME = "elapsed_time"

        // 计时器类型
        const val TIMER_TYPE_POMODORO = "pomodoro"
        const val TIMER_TYPE_REGULAR = "regular"
    }

    // 服务绑定器
    private val binder = TimerBinder()
    
    // 协程作用域
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    
    // 计时器
    private var countDownTimer: CountDownTimer? = null
    
    // 计时状态
    private var isTimerRunning = false
    private var timeRemainingMillis: Long = 0
    
    // 番茄钟状态
    private var currentPomodoroSession: PomodoroSession? = null
    
    // 正计时状态
    private var currentTimerRecord: TimerRecord? = null
    private var regularTimerStartTime: Long = 0
    private var regularTimerElapsedTime: Long = 0
    private var regularTimerUpdateJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 启动前台服务
        startForeground(NOTIFICATION_ID, createNotification("计时器运行中", "00:00"))
        return START_STICKY
    }

    override fun onDestroy() {
        stopTimer()
        super.onDestroy()
    }

    /**
     * 创建通知渠道
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * 创建通知
     */
    private fun createNotification(title: String, content: String): android.app.Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setContentIntent(pendingIntent)
            .build()
    }

    /**
     * 更新通知
     */
    private fun updateNotification(title: String, content: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(title, content))
    }

    /**
     * 启动番茄钟
     */
    fun startPomodoroTimer(session: PomodoroSession) {
        stopTimer() // 停止当前计时器
        
        currentPomodoroSession = session
        
        // 根据当前状态确定计时时长
        val durationMillis = when (session.currentStatus) {
            PomodoroStatus.WORKING -> TimeUnit.MINUTES.toMillis(session.workDuration.toLong())
            PomodoroStatus.SHORT_BREAK -> TimeUnit.MINUTES.toMillis(session.shortBreakDuration.toLong())
            PomodoroStatus.LONG_BREAK -> TimeUnit.MINUTES.toMillis(session.longBreakDuration.toLong())
            else -> TimeUnit.MINUTES.toMillis(session.workDuration.toLong())
        }
        
        timeRemainingMillis = durationMillis
        startCountDownTimer(durationMillis)
        
        // 更新通知
        val statusText = when (session.currentStatus) {
            PomodoroStatus.WORKING -> "工作中"
            PomodoroStatus.SHORT_BREAK -> "短休息"
            PomodoroStatus.LONG_BREAK -> "长休息"
            else -> "番茄钟"
        }
        updateNotification("番茄钟 - $statusText", formatTime(durationMillis))
        
        // 广播状态变化
        broadcastPomodoroStateChanged(session.currentStatus)
    }

    /**
     * 启动正计时
     */
    fun startRegularTimer(record: TimerRecord) {
        stopTimer() // 停止当前计时器
        
        currentTimerRecord = record
        regularTimerStartTime = System.currentTimeMillis() - record.totalPausedTime
        
        // 启动定期更新任务
        regularTimerUpdateJob = serviceScope.launch {
            while (true) {
                val elapsedTime = if (record.isPaused) {
                    record.duration
                } else {
                    System.currentTimeMillis() - regularTimerStartTime
                }
                regularTimerElapsedTime = elapsedTime
                
                // 更新通知
                updateNotification("正计时 - ${record.title}", formatTime(elapsedTime))
                
                // 广播计时更新
                broadcastRegularTimerTick(elapsedTime)
                
                kotlinx.coroutines.delay(1000) // 每秒更新一次
            }
        }
        
        isTimerRunning = true
    }

    /**
     * 暂停计时器
     */
    fun pauseTimer() {
        if (currentPomodoroSession != null) {
            countDownTimer?.cancel()
            currentPomodoroSession?.currentStatus = PomodoroStatus.PAUSED
            broadcastPomodoroStateChanged(PomodoroStatus.PAUSED)
        } else if (currentTimerRecord != null) {
            currentTimerRecord?.isPaused = true
            currentTimerRecord?.pauseStartTime = System.currentTimeMillis()
            regularTimerUpdateJob?.cancel()
        }
        
        isTimerRunning = false
        updateNotification("计时器已暂停", formatTime(timeRemainingMillis))
    }

    /**
     * 恢复计时器
     */
    fun resumeTimer() {
        if (currentPomodoroSession != null && currentPomodoroSession?.currentStatus == PomodoroStatus.PAUSED) {
            // 恢复番茄钟状态
            val previousStatus = if (currentPomodoroSession?.currentStatus == PomodoroStatus.PAUSED) {
                PomodoroStatus.WORKING // 默认恢复为工作状态
            } else {
                currentPomodoroSession?.currentStatus ?: PomodoroStatus.WORKING
            }
            currentPomodoroSession?.currentStatus = previousStatus
            
            // 重新启动倒计时
            startCountDownTimer(timeRemainingMillis)
            broadcastPomodoroStateChanged(previousStatus)
        } else if (currentTimerRecord != null && currentTimerRecord?.isPaused == true) {
            // 计算暂停时间
            val pauseDuration = System.currentTimeMillis() - (currentTimerRecord?.pauseStartTime ?: System.currentTimeMillis())
            currentTimerRecord?.totalPausedTime = (currentTimerRecord?.totalPausedTime ?: 0) + pauseDuration
            
            // 更新开始时间
            regularTimerStartTime = System.currentTimeMillis() - (currentTimerRecord?.duration ?: 0)
            
            // 恢复正计时
            currentTimerRecord?.isPaused = false
            currentTimerRecord?.pauseStartTime = null
            
            // 重新启动更新任务
            startRegularTimer(currentTimerRecord!!)
        }
        
        isTimerRunning = true
    }

    /**
     * 停止计时器
     */
    fun stopTimer() {
        countDownTimer?.cancel()
        regularTimerUpdateJob?.cancel()
        
        isTimerRunning = false
        timeRemainingMillis = 0
        
        currentPomodoroSession = null
        currentTimerRecord = null
    }

    /**
     * 启动倒计时计时器
     */
    private fun startCountDownTimer(durationMillis: Long) {
        countDownTimer?.cancel()
        
        countDownTimer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemainingMillis = millisUntilFinished
                
                // 更新通知
                val statusText = when (currentPomodoroSession?.currentStatus) {
                    PomodoroStatus.WORKING -> "工作中"
                    PomodoroStatus.SHORT_BREAK -> "短休息"
                    PomodoroStatus.LONG_BREAK -> "长休息"
                    else -> "番茄钟"
                }
                updateNotification("番茄钟 - $statusText", formatTime(millisUntilFinished))
                
                // 广播计时更新
                broadcastTimerTick(millisUntilFinished)
            }

            override fun onFinish() {
                timeRemainingMillis = 0
                
                // 广播计时结束
                broadcastTimerFinish()
                
                // 自动切换番茄钟状态
                currentPomodoroSession?.let { session ->
                    when (session.currentStatus) {
                        PomodoroStatus.WORKING -> {
                            // 工作结束，增加完成的番茄数
                            session.completedPomodoros++
                            
                            // 判断是进入短休息还是长休息
                            if (session.completedPomodoros % session.longBreakInterval == 0) {
                                // 进入长休息
                                session.currentStatus = PomodoroStatus.LONG_BREAK
                                startPomodoroTimer(session)
                            } else {
                                // 进入短休息
                                session.currentStatus = PomodoroStatus.SHORT_BREAK
                                startPomodoroTimer(session)
                            }
                        }
                        PomodoroStatus.SHORT_BREAK, PomodoroStatus.LONG_BREAK -> {
                            // 休息结束，进入工作状态
                            session.currentStatus = PomodoroStatus.WORKING
                            startPomodoroTimer(session)
                        }
                        else -> {
                            // 其他状态，默认进入工作状态
                            session.currentStatus = PomodoroStatus.WORKING
                            startPomodoroTimer(session)
                        }
                    }
                }
            }
        }.start()
        
        isTimerRunning = true
    }

    /**
     * 广播计时器更新
     */
    private fun broadcastTimerTick(millisUntilFinished: Long) {
        val intent = Intent(ACTION_TIMER_TICK).apply {
            putExtra(EXTRA_TIME_REMAINING, millisUntilFinished)
            putExtra(EXTRA_TIMER_TYPE, TIMER_TYPE_POMODORO)
        }
        sendBroadcast(intent)
    }

    /**
     * 广播正计时更新
     */
    private fun broadcastRegularTimerTick(elapsedTime: Long) {
        val intent = Intent(ACTION_TIMER_TICK).apply {
            putExtra(EXTRA_ELAPSED_TIME, elapsedTime)
            putExtra(EXTRA_TIMER_TYPE, TIMER_TYPE_REGULAR)
        }
        sendBroadcast(intent)
    }

    /**
     * 广播计时器结束
     */
    private fun broadcastTimerFinish() {
        val intent = Intent(ACTION_TIMER_FINISH).apply {
            putExtra(EXTRA_TIMER_TYPE, TIMER_TYPE_POMODORO)
        }
        sendBroadcast(intent)
    }

    /**
     * 广播番茄钟状态变化
     */
    private fun broadcastPomodoroStateChanged(status: PomodoroStatus) {
        val intent = Intent(ACTION_POMODORO_STATE_CHANGED).apply {
            putExtra(EXTRA_POMODORO_STATUS, status.name)
            putExtra(EXTRA_TIMER_TYPE, TIMER_TYPE_POMODORO)
        }
        sendBroadcast(intent)
    }

    /**
     * 格式化时间
     */
    private fun formatTime(millis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    /**
     * 服务绑定器
     */
    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }
}
