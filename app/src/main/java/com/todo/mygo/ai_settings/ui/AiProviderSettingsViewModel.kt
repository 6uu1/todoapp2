package com.todo.mygo.ai_settings.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.todo.mygo.ai_settings.data.AiProviderInfo
import com.todo.mygo.ai_settings.data.AiSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class AiProviderSettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AiSettingsRepository = AiSettingsRepository(application)

    private val _allProviderConfigs = MutableLiveData<List<AiProviderInfo>>()
    val allProviderConfigs: LiveData<List<AiProviderInfo>> = _allProviderConfigs

    private val _selectedProviderName = MutableLiveData<String?>()
    val selectedProviderName: LiveData<String?> = _selectedProviderName

    private val _currentProviderConfig = MutableLiveData<AiProviderInfo?>()
    val currentProviderConfig: LiveData<AiProviderInfo?> = _currentProviderConfig

    private val _testConnectionResult = MutableLiveData<Pair<Boolean, String>>() // Boolean: success, String: message
    val testConnectionResult: LiveData<Pair<Boolean, String>> = _testConnectionResult

    val providerNames: List<String> = AiSettingsRepository.DEFAULT_PROVIDERS.map { it.name }

    init {
        repository.initializeDefaultProviders() // 确保默认值已设置
        loadAllConfigs()
        loadSelectedProviderName()
    }

    private fun loadAllConfigs() {
        _allProviderConfigs.value = repository.getAllProviderConfigs()
    }

    private fun loadSelectedProviderName() {
        val name = repository.getSelectedProviderName()
        _selectedProviderName.value = name
        if (name != null) {
            loadProviderConfig(name)
        } else if (providerNames.isNotEmpty()) {
            // 如果没有选中的，默认选中第一个
            selectProvider(providerNames.first())
        }
    }

    fun loadProviderConfig(providerName: String) {
        _currentProviderConfig.value = repository.getProviderConfig(providerName)
    }

    fun selectProvider(providerName: String) {
        repository.saveSelectedProviderName(providerName)
        _selectedProviderName.value = providerName
        loadProviderConfig(providerName)
    }

    fun saveProviderConfig(providerInfo: AiProviderInfo) {
        repository.saveProviderConfig(providerInfo)
        // 更新当前配置和列表，以便UI可以立即反映更改
        _currentProviderConfig.value = providerInfo
        val updatedList = _allProviderConfigs.value?.map {
            if (it.name == providerInfo.name) providerInfo else it
        }
        _allProviderConfigs.value = updatedList ?: listOf(providerInfo)
    }

    fun testConnection(apiUrl: String, apiKey: String) {
        if (apiUrl.isBlank()) {
            _testConnectionResult.value = Pair(false, "API URL不能为空")
            return
        }
        // API Key 可以为空，某些API可能不需要，或者用户只是想保存URL
        // if (apiKey.isBlank()) {
        // _testConnectionResult.value = Pair(false, "API Key不能为空")
        // return
        // }

        viewModelScope.launch {
            _testConnectionResult.value = Pair(false, "正在测试连接...") // Show pending state
            try {
                val result = pingUrl(apiUrl) // 简化测试：仅ping URL
                if (result) {
                    _testConnectionResult.value = Pair(true, "连接成功")
                } else {
                    _testConnectionResult.value = Pair(false, "连接失败：无法访问URL或URL无效")
                }
            } catch (e: Exception) {
                _testConnectionResult.value = Pair(false, "连接测试失败: ${e.message}")
            }
        }
    }

    // 简单的URL Ping测试 (实际的AI API测试会更复杂)
    private suspend fun pingUrl(urlString: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET" // 或者 "HEAD"
            connection.connectTimeout = 5000 // 5秒超时
            connection.readTimeout = 5000
            connection.connect()
            val responseCode = connection.responseCode
            // 认为 2xx 或 3xx 是成功的初步迹象 (对于某些API，即使是401/403也意味着服务器可达)
            responseCode in 200..399 || responseCode == HttpURLConnection.HTTP_UNAUTHORIZED || responseCode == HttpURLConnection.HTTP_FORBIDDEN
        } catch (e: Exception) {
            // MalformedURLException, IOException, etc.
            false
        }
    }
}