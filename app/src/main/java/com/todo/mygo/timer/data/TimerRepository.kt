package com.todo.mygo.timer.data

import androidx.lifecycle.LiveData
import java.util.Calendar

/**
 * 计时器数据仓库，提供对计时器相关数据的访问
 */
class TimerRepository(private val timerDao: TimerDao) {

    // PomodoroSession 相关方法
    suspend fun insertPomodoroSession(session: PomodoroSession) {
        timerDao.insertPomodoroSession(session)
    }

    suspend fun updatePomodoroSession(session: PomodoroSession) {
        timerDao.updatePomodoroSession(session)
    }

    suspend fun deletePomodoroSession(session: PomodoroSession) {
        timerDao.deletePomodoroSession(session)
    }

    fun getLatestPomodoroSession(): LiveData<PomodoroSession?> {
        return timerDao.getLatestPomodoroSession()
    }

    fun getAllPomodoroSessions(): LiveData<List<PomodoroSession>> {
        return timerDao.getAllPomodoroSessions()
    }

    fun getPomodoroSessionsForToday(): LiveData<List<PomodoroSession>> {
        val calendar = Calendar.getInstance()
        // 设置为今天的开始
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        // 设置为今天的结束
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis

        return timerDao.getPomodoroSessionsForDay(startOfDay, endOfDay)
    }

    // TimerRecord 相关方法
    suspend fun insertTimerRecord(record: TimerRecord) {
        timerDao.insertTimerRecord(record)
    }

    suspend fun updateTimerRecord(record: TimerRecord) {
        timerDao.updateTimerRecord(record)
    }

    suspend fun deleteTimerRecord(record: TimerRecord) {
        timerDao.deleteTimerRecord(record)
    }

    fun getLatestTimerRecord(): LiveData<TimerRecord?> {
        return timerDao.getLatestTimerRecord()
    }

    fun getAllTimerRecords(): LiveData<List<TimerRecord>> {
        return timerDao.getAllTimerRecords()
    }

    fun getTimerRecordsForToday(): LiveData<List<TimerRecord>> {
        val calendar = Calendar.getInstance()
        // 设置为今天的开始
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        // 设置为今天的结束
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis

        return timerDao.getTimerRecordsForDay(startOfDay, endOfDay)
    }

    // Reflection 相关方法
    suspend fun insertReflection(reflection: Reflection) {
        timerDao.insertReflection(reflection)
    }

    suspend fun updateReflection(reflection: Reflection) {
        timerDao.updateReflection(reflection)
    }

    suspend fun deleteReflection(reflection: Reflection) {
        timerDao.deleteReflection(reflection)
    }

    fun getAllReflections(): LiveData<List<Reflection>> {
        return timerDao.getAllReflections()
    }

    fun getReflectionsForToday(): LiveData<List<Reflection>> {
        val calendar = Calendar.getInstance()
        // 设置为今天的开始
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        // 设置为今天的结束
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis

        return timerDao.getReflectionsForDay(startOfDay, endOfDay)
    }
}
