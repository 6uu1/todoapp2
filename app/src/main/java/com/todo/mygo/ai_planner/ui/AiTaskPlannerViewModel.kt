package com.todo.mygo.ai_planner.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.todo.mygo.ai_settings.data.AiProviderInfo
import com.todo.mygo.ai_settings.data.AiSettingsRepository
import com.todo.mygo.calendar.data.CalendarDatabase // Added import
import com.todo.mygo.calendar.data.CalendarRepository // Added import
import com.todo.mygo.gantt.data.PlannedTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.Calendar // Added import
import java.util.UUID
import java.util.concurrent.TimeUnit

class AiTaskPlannerViewModel(application: Application) : AndroidViewModel(application) {

    private val aiSettingsRepository = AiSettingsRepository(application)
    // Initialize CalendarRepository for saving tasks
    private val calendarRepository: CalendarRepository
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()

    private val _plannedTasks = MutableLiveData<List<PlannedTask>>()
    val plannedTasks: LiveData<List<PlannedTask>> = _plannedTasks

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _saveStatus = MutableLiveData<String?>()
    val saveStatus: LiveData<String?> = _saveStatus

    init {
        val database = CalendarDatabase.getDatabase(application)
        calendarRepository = CalendarRepository(database.eventDao(), database.plannedTaskDao(), database.todoDao())
    }

    fun planTasks(goal: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val selectedProviderName = aiSettingsRepository.getSelectedProviderName()
                if (selectedProviderName == null) {
                    _error.value = "尚未配置AI提供商"
                    _isLoading.value = false
                    return@launch
                }

                val providerConfig = aiSettingsRepository.getProviderConfig(selectedProviderName)
                if (providerConfig == null || providerConfig.apiKey.isBlank() || providerConfig.apiUrl.isBlank()) {
                    _error.value = "AI提供商配置不完整 (API Key 或 URL缺失)"
                    _isLoading.value = false
                    return@launch
                }

                val tasks = callAiToPlanTasks(goal, providerConfig)
                _plannedTasks.postValue(tasks)

            } catch (e: IOException) {
                _error.value = "网络请求失败: ${e.message}"
            } catch (e: AiApiException) {
                _error.value = "AI API 错误: ${e.message}"
            } catch (e: JsonSyntaxException) {
                _error.value = "解析AI响应失败: ${e.message}"
            } catch (e: Exception) {
                _error.value = "发生未知错误: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun callAiToPlanTasks(goal: String, providerInfo: AiProviderInfo): List<PlannedTask> {
        return withContext(Dispatchers.IO) {
            val apiUrl: String
            // Removed direct dependency on providerInfo.modelId
            // Default model for OpenAI and Custom (OpenAI-compatible)
            val modelToUse: String = "gpt-3.5-turbo"

            // Determine API URL based on provider type
            if (providerInfo.name.equals("OpenAI", ignoreCase = true)) {
                apiUrl = "https://api.openai.com/v1/chat/completions"
            } else if (providerInfo.name.equals("Custom", ignoreCase = true)) {
                // For custom, ensure the URL is the base and append the chat completions path if not already there.
                // This is a simple check; more robust URL handling might be needed.
                apiUrl = if (providerInfo.apiUrl.endsWith("/v1/chat/completions")) {
                    providerInfo.apiUrl
                } else {
                    providerInfo.apiUrl.trimEnd('/') + "/v1/chat/completions"
                }
            } else {
                // Potentially handle other providers or throw an error for unsupported ones
                throw AiApiException("Unsupported AI provider: ${providerInfo.name}")
            }

            val systemPrompt = """
            You are an expert project planner. Your goal is to break down a user's large goal into a list of smaller, actionable tasks.
            For each task, provide a name, and EITHER a start_date_offset_days and end_date_offset_days (relative to today, where 0 is today), OR a duration_hours.
            Respond strictly in JSON format. The JSON should be an object with a single key 'tasks', which is an array of task objects.
            Each task object must have a 'task_name' (string).
            It must also have EITHER ('start_date_offset_days' (integer) AND 'end_date_offset_days' (integer)) OR 'duration_hours' (integer).
            Do not include any other text or explanations outside the JSON structure.
            Example task: {"task_name": "Draft initial proposal", "duration_hours": 4}
            Example task with offset: {"task_name": "Review feedback", "start_date_offset_days": 1, "end_date_offset_days": 2}
            """.trimIndent()

            val userPrompt = "My large goal is: \"$goal\". Please break this down into smaller tasks as per your instructions."

            val messages = listOf(
                OpenAiChatMessage(role = "system", content = systemPrompt),
                OpenAiChatMessage(role = "user", content = userPrompt)
            )

            val openAiRequest = OpenAiChatRequest(
                model = modelToUse,
                messages = messages,
                response_format = mapOf("type" to "json_object") // Request JSON response
            )

            val requestBodyJson = gson.toJson(openAiRequest)

            val request = Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer ${providerInfo.apiKey}")
                .addHeader("Content-Type", "application/json")
                .post(requestBodyJson.toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                val errorBody = response.body?.string()
                // Log more detailed error if possible
                // Log.e("AiTaskPlannerVM", "API Error: ${response.code} ${response.message} - Body: $errorBody")
                throw AiApiException("API请求失败: ${response.code} ${response.message}. ${errorBody ?: ""}")
            }

            val responseBody = response.body?.string()
            if (responseBody.isNullOrBlank()) {
                throw AiApiException("AI响应为空")
            }

            // Log.d("AiTaskPlannerVM", "Raw AI Response: $responseBody")

            val openAiResponse = try {
                gson.fromJson(responseBody, OpenAiChatResponse::class.java)
            } catch (e: JsonSyntaxException) {
                // Log.e("AiTaskPlannerVM", "Failed to parse main OpenAI response: $responseBody", e)
                throw AiApiException("解析AI主响应失败: ${e.message}. Response: $responseBody")
            }


            val messageContent = openAiResponse?.choices?.firstOrNull()?.message?.content
            if (messageContent.isNullOrBlank()) {
                // Log.w("AiTaskPlannerVM", "AI response message content is null or blank. Full response: $responseBody")
                throw AiApiException("AI响应中未找到有效内容。")
            }
            
            // Log.d("AiTaskPlannerVM", "AI Message Content (JSON String): $messageContent")

            val tasksContainer = try {
                gson.fromJson(messageContent, AiPlannedTasksContainer::class.java)
            } catch (e: JsonSyntaxException) {
                // Log.e("AiTaskPlannerVM", "Failed to parse tasks JSON from AI message content: $messageContent", e)
                throw AiApiException("解析AI任务列表JSON失败: ${e.message}. Content: $messageContent")
            }

            convertAiResponseToPlannedTasks(tasksContainer)
        }
    }

    private fun convertAiResponseToPlannedTasks(aiTasksContainer: AiPlannedTasksContainer): List<PlannedTask> {
        val plannedTasks = mutableListOf<PlannedTask>()
        val today = Calendar.getInstance()
        
        // Set today to the beginning of the day for consistent offset calculations
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        val todayStartMillis = today.timeInMillis

        var lastEndTime = todayStartMillis // Used for sequencing tasks that only have duration

        aiTasksContainer.tasks.forEach { aiTask ->
            if (aiTask.task_name.isBlank()) return@forEach // Skip tasks without a name

            val name = aiTask.task_name
            var startTime: Long
            var endTime: Long

            when {
                // Case 1: start_date_offset_days and end_date_offset_days are provided
                aiTask.start_date_offset_days != null && aiTask.end_date_offset_days != null -> {
                    if (aiTask.start_date_offset_days < 0 || aiTask.end_date_offset_days < aiTask.start_date_offset_days) {
                        // Invalid offset, skip or log error, for now, we'll try to make it a 1-day task from start_offset
                        // Log.w("AiTaskPlannerVM","Invalid date offsets for task '${aiTask.task_name}'. Start: ${aiTask.start_date_offset_days}, End: ${aiTask.end_date_offset_days}")
                        val startCal = Calendar.getInstance().apply { timeInMillis = todayStartMillis }
                        startCal.add(Calendar.DAY_OF_YEAR, aiTask.start_date_offset_days.coerceAtLeast(0))
                        startTime = startCal.timeInMillis
                        
                        val endCal = Calendar.getInstance().apply { timeInMillis = startTime }
                        endCal.add(Calendar.DAY_OF_YEAR, 1) // Default to 1 day duration if end is invalid
                        endTime = endCal.timeInMillis -1 // End of the day
                    } else {
                        val startCal = Calendar.getInstance().apply { timeInMillis = todayStartMillis }
                        startCal.add(Calendar.DAY_OF_YEAR, aiTask.start_date_offset_days)
                        startTime = startCal.timeInMillis

                        val endCal = Calendar.getInstance().apply { timeInMillis = todayStartMillis }
                        endCal.add(Calendar.DAY_OF_YEAR, aiTask.end_date_offset_days)
                        // Ensure end date is at the end of the day for full-day tasks
                        endCal.set(Calendar.HOUR_OF_DAY, 23)
                        endCal.set(Calendar.MINUTE, 59)
                        endCal.set(Calendar.SECOND, 59)
                        endCal.set(Calendar.MILLISECOND, 999)
                        endTime = endCal.timeInMillis

                        if (endTime < startTime) { // Should not happen if previous check is fine, but as a safeguard
                            endTime = startTime + (24 * 60 * 60 * 1000L -1) // Make it a 1 day task
                        }
                    }
                     // For tasks with explicit start/end, they don't necessarily affect lastEndTime for sequential duration-only tasks
                }
                // Case 2: Only duration_hours is provided
                aiTask.duration_hours != null && aiTask.duration_hours > 0 -> {
                    startTime = lastEndTime // Start after the previous duration-based task
                    val durationMillis = aiTask.duration_hours * 60 * 60 * 1000L
                    endTime = startTime + durationMillis
                    lastEndTime = endTime // Update for the next duration-based task
                }
                // Case 3: Insufficient information (e.g., only name, or invalid duration)
                else -> {
                    // Default: make it a 1-hour task starting from the last known end time or now
                    // Log.w("AiTaskPlannerVM","Task '${aiTask.task_name}' has insufficient time info. Defaulting to 1 hour duration.")
                    startTime = lastEndTime
                    val durationMillis = 1 * 60 * 60 * 1000L // Default to 1 hour
                    endTime = startTime + durationMillis
                    lastEndTime = endTime
                }
            }

            plannedTasks.add(
                PlannedTask(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    startTime = startTime,
                    endTime = endTime
                    // parentId, dependencyIds, etc., remain default
                )
            )
        }
        return plannedTasks.sortedBy { it.startTime } // Sort tasks by start time
    }

    fun clearError() {
        _error.value = null
    }

    fun savePlannedTasksToGantt() {
        val tasksToSave = _plannedTasks.value
        if (tasksToSave.isNullOrEmpty()) {
            _saveStatus.value = "没有要保存的任务。"
            return
        }

        viewModelScope.launch {
            try {
                calendarRepository.insertPlannedTasks(tasksToSave)
                _saveStatus.value = "任务已成功保存到甘特图！"
            } catch (e: Exception) {
                // Log.e("AiTaskPlannerVM", "Error saving tasks", e)
                _saveStatus.value = "保存任务失败: ${e.message}"
            }
        }
    }

    fun clearSaveStatus() {
        _saveStatus.value = null
    }
}

// Data classes for OpenAI Chat Completions API
data class OpenAiChatMessage(
    val role: String,
    val content: String
)

data class OpenAiChatRequest(
    val model: String = "gpt-3.5-turbo",
    val messages: List<OpenAiChatMessage>,
    val response_format: Map<String, String>? = null // e.g. mapOf("type" to "json_object")
)

data class OpenAiResponseMessage(
    val role: String?,
    val content: String?
)

data class OpenAiChoice(
    val index: Int?,
    val message: OpenAiResponseMessage?,
    val finish_reason: String?
)

data class OpenAiUsage(
    val prompt_tokens: Int?,
    val completion_tokens: Int?,
    val total_tokens: Int?
)

data class OpenAiChatResponse(
    val id: String?,
    val `object`: String?, // 'object' is a keyword in Kotlin, use backticks
    val created: Long?,
    val model: String?,
    val choices: List<OpenAiChoice>?,
    val usage: OpenAiUsage?,
    val system_fingerprint: String?
)

// For parsing the JSON content from AI's message
data class AiPlannedTaskItem(
    val task_name: String,
    val start_date_offset_days: Int? = null,
    val end_date_offset_days: Int? = null,
    val duration_hours: Int? = null
)

data class AiPlannedTasksContainer(
    val tasks: List<AiPlannedTaskItem>
)

class AiApiException(message: String) : Exception(message)