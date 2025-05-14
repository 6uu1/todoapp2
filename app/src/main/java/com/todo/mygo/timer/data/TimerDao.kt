package com.todo.mygo.timer.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface TimerDao {
    // PomodoroSession 相关操作
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPomodoroSession(session: PomodoroSession)

    @Update
    suspend fun updatePomodoroSession(session: PomodoroSession)

    @Delete
    suspend fun deletePomodoroSession(session: PomodoroSession)

    @Query("SELECT * FROM pomodoro_sessions ORDER BY startTime DESC LIMIT 1")
    fun getLatestPomodoroSession(): LiveData<PomodoroSession?>

    @Query("SELECT * FROM pomodoro_sessions ORDER BY startTime DESC")
    fun getAllPomodoroSessions(): LiveData<List<PomodoroSession>>

    @Query("SELECT * FROM pomodoro_sessions WHERE startTime >= :startOfDay AND startTime <= :endOfDay ORDER BY startTime DESC")
    fun getPomodoroSessionsForDay(startOfDay: Long, endOfDay: Long): LiveData<List<PomodoroSession>>

    // TimerRecord 相关操作
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimerRecord(record: TimerRecord)

    @Update
    suspend fun updateTimerRecord(record: TimerRecord)

    @Delete
    suspend fun deleteTimerRecord(record: TimerRecord)

    @Query("SELECT * FROM timer_records ORDER BY startTime DESC LIMIT 1")
    fun getLatestTimerRecord(): LiveData<TimerRecord?>

    @Query("SELECT * FROM timer_records ORDER BY startTime DESC")
    fun getAllTimerRecords(): LiveData<List<TimerRecord>>

    @Query("SELECT * FROM timer_records WHERE startTime >= :startOfDay AND startTime <= :endOfDay ORDER BY startTime DESC")
    fun getTimerRecordsForDay(startOfDay: Long, endOfDay: Long): LiveData<List<TimerRecord>>

    // Reflection 相关操作
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReflection(reflection: Reflection)

    @Update
    suspend fun updateReflection(reflection: Reflection)

    @Delete
    suspend fun deleteReflection(reflection: Reflection)

    @Query("SELECT * FROM reflections ORDER BY creationTime DESC")
    fun getAllReflections(): LiveData<List<Reflection>>

    @Query("SELECT * FROM reflections WHERE associatedDate >= :startOfDay AND associatedDate <= :endOfDay ORDER BY creationTime DESC")
    fun getReflectionsForDay(startOfDay: Long, endOfDay: Long): LiveData<List<Reflection>>
}
