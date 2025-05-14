package com.todo.mygo.timer.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.todo.mygo.calendar.data.CalendarDatabase
import com.todo.mygo.timer.data.Reflection
import com.todo.mygo.timer.data.TimerRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 反思记录 ViewModel，处理反思记录的增删改查
 */
class ReflectionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TimerRepository
    val todayReflections: LiveData<List<Reflection>>
    val allReflections: LiveData<List<Reflection>>
    
    private val _currentReflection = MutableLiveData<Reflection?>()
    val currentReflection: LiveData<Reflection?> = _currentReflection
    
    private val _saveStatus = MutableLiveData<Event<String>>()
    val saveStatus: LiveData<Event<String>> = _saveStatus

    init {
        val database = CalendarDatabase.getDatabase(application)
        repository = TimerRepository(database.timerDao())
        
        todayReflections = repository.getReflectionsForToday()
        allReflections = repository.getAllReflections()
    }

    /**
     * 创建新的反思记录
     */
    fun createNewReflection() {
        _currentReflection.value = Reflection(
            content = "",
            creationTime = System.currentTimeMillis(),
            lastModifiedTime = System.currentTimeMillis(),
            associatedDate = System.currentTimeMillis()
        )
    }

    /**
     * 编辑现有的反思记录
     */
    fun editReflection(reflection: Reflection) {
        _currentReflection.value = reflection
    }

    /**
     * 保存反思记录
     */
    fun saveReflection(content: String) {
        if (content.isBlank()) {
            _saveStatus.value = Event("反思内容不能为空")
            return
        }
        
        viewModelScope.launch {
            val reflection = _currentReflection.value?.copy(
                content = content,
                lastModifiedTime = System.currentTimeMillis()
            ) ?: Reflection(
                content = content,
                creationTime = System.currentTimeMillis(),
                lastModifiedTime = System.currentTimeMillis(),
                associatedDate = System.currentTimeMillis()
            )
            
            repository.insertReflection(reflection)
            _saveStatus.value = Event("反思已保存")
            _currentReflection.value = null
        }
    }

    /**
     * 删除反思记录
     */
    fun deleteReflection(reflection: Reflection) {
        viewModelScope.launch {
            repository.deleteReflection(reflection)
            
            if (_currentReflection.value?.id == reflection.id) {
                _currentReflection.value = null
            }
            
            _saveStatus.value = Event("反思已删除")
        }
    }

    /**
     * 取消编辑
     */
    fun cancelEdit() {
        _currentReflection.value = null
    }

    /**
     * 格式化日期
     */
    fun formatDate(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }
}

/**
 * 用于一次性事件的包装类
 */
class Event<T>(private val content: T) {
    
    private var hasBeenHandled = false
    
    /**
     * 返回内容并防止其再次使用
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }
    
    /**
     * 返回内容，即使已经处理过
     */
    fun peekContent(): T = content
}
