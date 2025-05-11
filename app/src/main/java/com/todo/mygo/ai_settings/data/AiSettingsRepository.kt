package com.todo.mygo.ai_settings.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AiSettingsRepository(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "ai_settings_prefs"
        private const val KEY_SELECTED_PROVIDER_NAME = "selected_ai_provider_name"
        private const val KEY_PROVIDER_CONFIG_PREFIX = "ai_provider_config_"

        // Default provider names
        const val PROVIDER_GEMINI = "Gemini"
        const val PROVIDER_AZURE_OPENAI = "Azure OpenAI"
        const val PROVIDER_DEEPSEEK = "DeepSeek"
        const val PROVIDER_OPENAI = "OpenAI"
        const val PROVIDER_CUSTOM = "Custom"

        val DEFAULT_PROVIDERS = listOf(
            AiProviderInfo(PROVIDER_GEMINI, "https://generativelanguage.googleapis.com/", ""),
            AiProviderInfo(PROVIDER_AZURE_OPENAI, "YOUR_AZURE_OPENAI_ENDPOINT", ""), // 用户需要替换
            AiProviderInfo(PROVIDER_DEEPSEEK, "https://api.deepseek.com/", ""),
            AiProviderInfo(PROVIDER_OPENAI, "https://api.openai.com/", ""),
            AiProviderInfo(PROVIDER_CUSTOM, "", "")
        )
    }

    fun saveSelectedProviderName(providerName: String) {
        sharedPreferences.edit().putString(KEY_SELECTED_PROVIDER_NAME, providerName).apply()
    }

    fun getSelectedProviderName(): String? {
        return sharedPreferences.getString(KEY_SELECTED_PROVIDER_NAME, DEFAULT_PROVIDERS.first().name)
    }

    fun saveProviderConfig(providerInfo: AiProviderInfo) {
        val jsonString = gson.toJson(providerInfo)
        sharedPreferences.edit().putString(KEY_PROVIDER_CONFIG_PREFIX + providerInfo.name, jsonString).apply()
    }

    fun getProviderConfig(providerName: String): AiProviderInfo? {
        val jsonString = sharedPreferences.getString(KEY_PROVIDER_CONFIG_PREFIX + providerName, null)
        return if (jsonString != null) {
            gson.fromJson(jsonString, AiProviderInfo::class.java)
        } else {
            // 如果没有保存的配置，返回默认配置（如果存在）
            DEFAULT_PROVIDERS.find { it.name == providerName }
        }
    }

    fun getAllProviderConfigs(): List<AiProviderInfo> {
        return DEFAULT_PROVIDERS.map { defaultProvider ->
            getProviderConfig(defaultProvider.name) ?: defaultProvider
        }
    }

    // 初始化时可以保存所有默认提供商的初始（空API Key）配置
    fun initializeDefaultProviders() {
        DEFAULT_PROVIDERS.forEach { provider ->
            if (getProviderConfig(provider.name) == null) { // 只有当没有保存过这个provider的配置时才保存
                saveProviderConfig(provider)
            }
        }
        // 确保至少有一个选定的提供商，如果没有，则选择第一个默认提供商
        if (getSelectedProviderName() == null && DEFAULT_PROVIDERS.isNotEmpty()) {
            saveSelectedProviderName(DEFAULT_PROVIDERS.first().name)
        }
    }
}