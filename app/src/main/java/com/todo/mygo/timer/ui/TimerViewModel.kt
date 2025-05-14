package com.todo.mygo.timer.ui

import android.app.Application
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.todo.mygo.calendar.data.CalendarDatabase
import com.todo.mygo.timer.data.PomodoroSession
import com.todo.mygo.timer.data.PomodoroStatus
import com.todo.mygo.timer.data.TimerRecord
import com.todo.mygo.timer.data.TimerRepository
import com.todo.mygo.timer.service.TimerService
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * 计时器 ViewModel，处理计时器逻辑和状态管理
 */
class TimerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TimerRepository
    
    // 服务连接
    private var timerService: TimerService? = null
    private var bound = false
    
    // 计时器状态
    private val _timerMode = MutableLiveData<TimerMode>(TimerMode.POMODORO)
    val timerMode: LiveData<TimerMode> = _timerMode
    
    private val _timerState = MutableLiveData<TimerState>(TimerState.IDLE)
    val timerState: LiveData<TimerState> = _timerState
    
    private val _timeRemaining = MutableLiveData<Long>(0)
    val timeRemaining: LiveData<Long> = _timeRemaining
    
    private val _elapsedTime = MutableLiveData<Long>(0)
    val elapsedTime: LiveData<Long> = _elapsedTime
    
    private val _pomodoroStatus = MutableLiveData<PomodoroStatus>(PomodoroStatus.IDLE)
    val pomodoroStatus: LiveData<PomodoroStatus> = _pomodoroStatus
    
    private val _completedPomodoros = MutableLiveData<Int>(0)
    val completedPomodoros: LiveData<Int> = _completedPomodoros
    
    // 当前会话
    private var currentPomodoroSession: PomodoroSession? = null
    private var currentTimerRecord: TimerRecord? = null
    
    // 默认设置
    private val defaultWorkDuration = 25 // 默认工作时长（分钟）
    private val defaultShortBreakDuration = 5 // 默认短休息时长（分钟）
    private val defaultLongBreakDuration = 15 // 默认长休息时长（分钟）
    private val defaultLongBreakInterval = 4 // 默认长休息间隔

    // 服务连接回调
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TimerService.TimerBinder
            timerService = binder.getService()
            bound = true
            
            // 恢复状态
            restoreTimerState()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            timerService = null
            bound = false
        }
    }
    
    // 广播接收器
    private val timerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                TimerService.ACTION_TIMER_TICK -> {
                    val timerType = intent.getStringExtra(TimerService.EXTRA_TIMER_TYPE)
                    if (timerType == TimerService.TIMER_TYPE_POMODORO) {
                        val timeRemaining = intent.getLongExtra(TimerService.EXTRA_TIME_REMAINING, 0)
                        _timeRemaining.value = timeRemaining
                    } else if (timerType == TimerService.TIMER_TYPE_REGULAR) {
                        val elapsedTime = intent.getLongExtra(TimerService.EXTRA_ELAPSED_TIME, 0)
                        _elapsedTime.value = elapsedTime
                    }
                }
                TimerService.ACTION_TIMER_FINISH -> {
                    _timerState.value = TimerState.FINISHED
                }
                TimerService.ACTION_POMODORO_STATE_CHANGED -> {
                    val statusName = intent.getStringExtra(TimerService.EXTRA_POMODORO_STATUS)
                    statusName?.let {
                        val status = PomodoroStatus.valueOf(it)
                        _pomodoroStatus.value = status
                        
                        if (status == PomodoroStatus.WORKING) {
                            _timerState.value = TimerState.RUNNING
                        } else if (status == PomodoroStatus.PAUSED) {
                            _timerState.value = TimerState.PAUSED
                        }
                    }
                }
            }
        }
    }

    init {
        val database = CalendarDatabase.getDatabase(application)
        repository = TimerRepository(database.timerDao())
        
        // 注册广播接收器
        val filter = IntentFilter().apply {
            addAction(TimerService.ACTION_TIMER_TICK)
            addAction(TimerService.ACTION_TIMER_FINISH)
            addAction(TimerService.ACTION_POMODORO_STATE_CHANGED)
        }
        application.registerReceiver(timerReceiver, filter)
        
        // 绑定服务
        val intent = Intent(application, TimerService::class.java)
        application.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun onCleared() {
        // 解绑服务
        if (bound) {
            getApplication<Application>().unbindService(connection)
            bound = false
        }
        
        // 注销广播接收器
        getApplication<Application>().unregisterReceiver(timerReceiver)
        
        super.onCleared()
    }

    /**
     * 设置计时器模式
     */
    fun setTimerMode(mode: TimerMode) {
        if (_timerMode.value != mode) {
            _timerMode.value = mode
            
            // 重置计时器状态
            resetTimer()
        }
    }

    /**
     * 开始计时
     */
    fun startTimer() {
        if (_timerMode.value == TimerMode.POMODORO) {
            startPomodoroTimer()
        } else {
            startRegularTimer()
        }
    }

    /**
     * 开始番茄钟
     */
    private fun startPomodoroTimer() {
        if (timerService == null || !bound) return
        
        // 创建新的番茄钟会话
        if (currentPomodoroSession == null || _timerState.value == TimerState.IDLE || _timerState.value == TimerState.FINISHED) {
            currentPomodoroSession = PomodoroSession(
                workDuration = defaultWorkDuration,
                shortBreakDuration = defaultShortBreakDuration,
                longBreakDuration = defaultLongBreakDuration,
                longBreakInterval = defaultLongBreakInterval,
                currentStatus = PomodoroStatus.WORKING
            )
            
            // 保存到数据库
            viewModelScope.launch {
                repository.insertPomodoroSession(currentPomodoroSession!!)
            }
        }
        
        // 启动番茄钟
        currentPomodoroSession?.let {
            timerService?.startPomodoroTimer(it)
            _timerState.value = TimerState.RUNNING
            _pomodoroStatus.value = it.currentStatus
            _completedPomodoros.value = it.completedPomodoros
        }
    }

    /**
     * 开始正计时
     */
    private fun startRegularTimer() {
        if (timerService == null || !bound) return
        
        // 创建新的正计时记录
        if (currentTimerRecord == null || _timerState.value == TimerState.IDLE || _timerState.value == TimerState.FINISHED) {
            currentTimerRecord = TimerRecord(
                title = "计时记录",
                description = "正计时记录"
            )
            
            // 保存到数据库
            viewModelScope.launch {
                repository.insertTimerRecord(currentTimerRecord!!)
            }
        }
        
        // 启动正计时
        currentTimerRecord?.let {
            timerService?.startRegularTimer(it)
            _timerState.value = TimerState.RUNNING
        }
    }

    /**
     * 暂停计时
     */
    fun pauseTimer() {
        if (timerService == null || !bound) return
        
        timerService?.pauseTimer()
        _timerState.value = TimerState.PAUSED
        
        // 更新数据库
        updateTimerRecord()
    }

    /**
     * 恢复计时
     */
    fun resumeTimer() {
        if (timerService == null || !bound) return
        
        timerService?.resumeTimer()
        _timerState.value = TimerState.RUNNING
        
        // 更新数据库
        updateTimerRecord()
    }

    /**
     * 停止计时
     */
    fun stopTimer() {
        if (timerService == null || !bound) return
        
        timerService?.stopTimer()
        _timerState.value = TimerState.IDLE
        
        // 更新数据库
        finishTimerRecord()
        
        // 重置状态
        resetTimer()
    }

    /**
     * 跳过当前阶段
     */
    fun skipCurrentPhase() {
        if (_timerMode.value != TimerMode.POMODORO || timerService == null || !bound) return
        
        // 根据当前状态决定下一个状态
        currentPomodoroSession?.let { session ->
            when (session.currentStatus) {
                PomodoroStatus.WORKING -> {
                    // 工作阶段结束，增加完成的番茄数
                    session.completedPomodoros++
                    _completedPomodoros.value = session.completedPomodoros
                    
                    // 判断是进入短休息还是长休息
                    if (session.completedPomodoros % session.longBreakInterval == 0) {
                        // 进入长休息
                        session.currentStatus = PomodoroStatus.LONG_BREAK
                    } else {
                        // 进入短休息
                        session.currentStatus = PomodoroStatus.SHORT_BREAK
                    }
                }
                PomodoroStatus.SHORT_BREAK, PomodoroStatus.LONG_BREAK -> {
                    // 休息阶段结束，进入工作阶段
                    session.currentStatus = PomodoroStatus.WORKING
                }
                else -> {
                    // 其他状态，默认进入工作阶段
                    session.currentStatus = PomodoroStatus.WORKING
                }
            }
            
            // 更新数据库
            viewModelScope.launch {
                repository.updatePomodoroSession(session)
            }
            
            // 重新启动计时器
            timerService?.startPomodoroTimer(session)
            _pomodoroStatus.value = session.currentStatus
        }
    }

    /**
     * 重置计时器
     */
    private fun resetTimer() {
        _timeRemaining.value = 0L
        _elapsedTime.value = 0L
        _pomodoroStatus.value = PomodoroStatus.IDLE
        _completedPomodoros.value = 0
        
        currentPomodoroSession = null
        currentTimerRecord = null
    }

    /**
     * 恢复计时器状态
     */
    private fun restoreTimerState() {
        // 从数据库加载最新的番茄钟会话或正计时记录
        viewModelScope.launch {
            if (_timerMode.value == TimerMode.POMODORO) {
                repository.getLatestPomodoroSession().observeForever { session ->
                    session?.let {
                        if (it.endTime == null) {
                            // 未完成的会话，恢复状态
                            currentPomodoroSession = it
                            _pomodoroStatus.value = it.currentStatus
                            _completedPomodoros.value = it.completedPomodoros
                            
                            // 如果状态不是空闲，则恢复计时
                            if (it.currentStatus != PomodoroStatus.IDLE) {
                                timerService?.startPomodoroTimer(it)
                                _timerState.value = TimerState.RUNNING
                            }
                        }
                    }
                }
            } else {
                repository.getLatestTimerRecord().observeForever { record ->
                    record?.let {
                        if (it.endTime == null) {
                            // 未完成的记录，恢复状态
                            currentTimerRecord = it
                            
                            // 如果不是暂停状态，则恢复计时
                            if (!it.isPaused) {
                                timerService?.startRegularTimer(it)
                                _timerState.value = TimerState.RUNNING
                            } else {
                                _timerState.value = TimerState.PAUSED
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 更新计时记录
     */
    private fun updateTimerRecord() {
        viewModelScope.launch {
            currentPomodoroSession?.let {
                repository.updatePomodoroSession(it)
            }
            
            currentTimerRecord?.let {
                if (_timerState.value == TimerState.PAUSED) {
                    it.isPaused = true
                    it.pauseStartTime = System.currentTimeMillis()
                } else {
                    it.isPaused = false
                    it.pauseStartTime?.let { pauseTime ->
                        it.totalPausedTime += System.currentTimeMillis() - pauseTime
                    }
                    it.pauseStartTime = null
                }
                
                it.duration = _elapsedTime.value ?: 0
                repository.updateTimerRecord(it)
            }
        }
    }

    /**
     * 完成计时记录
     */
    private fun finishTimerRecord() {
        viewModelScope.launch {
            currentPomodoroSession?.let {
                it.endTime = System.currentTimeMillis()
                repository.updatePomodoroSession(it)
            }
            
            currentTimerRecord?.let {
                it.endTime = System.currentTimeMillis()
                it.duration = _elapsedTime.value ?: 0
                repository.updateTimerRecord(it)
            }
        }
    }

    /**
     * 格式化时间
     */
    fun formatTime(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
}

/**
 * 计时器模式
 */
enum class TimerMode {
    POMODORO, // 番茄钟
    REGULAR   // 正计时
}

/**
 * 计时器状态
 */
enum class TimerState {
    IDLE,     // 空闲
    RUNNING,  // 运行中
    PAUSED,   // 暂停
    FINISHED  // 完成
}
