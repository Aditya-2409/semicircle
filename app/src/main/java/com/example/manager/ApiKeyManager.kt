package com.example.manager

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray

class ApiKeyManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("semicircle_api_keys", Context.MODE_PRIVATE)

    private val _apiKeys = MutableStateFlow<List<String>>(emptyList())
    val apiKeys: StateFlow<List<String>> = _apiKeys.asStateFlow()

    private val _activeKeyIndex = MutableStateFlow(0)
    val activeKeyIndex: StateFlow<Int> = _activeKeyIndex.asStateFlow()

    private val _lastQuotaExhaustedMessage = MutableStateFlow<String?>(null)
    val lastQuotaExhaustedMessage: StateFlow<String?> = _lastQuotaExhaustedMessage.asStateFlow()

    init {
        loadKeys()
    }

    private fun loadKeys() {
        val saved = prefs.getString("backup_keys_json", null)
        val list = mutableListOf<String>()

        val defaultKey = "AIzaSyAJJ5NPDoCcKMMe01YOYcKbZ2O4nBiWJY4"
        list.add(defaultKey)

        // Check if BuildConfig has a bundled GEMINI_API_KEY
        val buildKey = try {
            val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
            field.get(null) as? String
        } catch (_: Exception) {
            null
        }

        if (!buildKey.isNullOrBlank() && !list.contains(buildKey)) {
            list.add(buildKey)
        }

        if (saved != null) {
            try {
                val jsonArray = JSONArray(saved)
                for (i in 0 until jsonArray.length()) {
                    val key = jsonArray.getString(i).trim()
                    if (key.isNotEmpty() && !list.contains(key)) {
                        list.add(key)
                    }
                }
            } catch (_: Exception) {
                // Ignore parse errors
            }
        }

        _apiKeys.value = list
        val savedIndex = prefs.getInt("active_key_index", 0)
        _activeKeyIndex.value = if (savedIndex in list.indices) savedIndex else 0
    }

    fun getActiveKey(): String? {
        val keys = _apiKeys.value
        val idx = _activeKeyIndex.value
        return if (idx in keys.indices) keys[idx] else null
    }

    fun addSubstituteKey(newKey: String): Boolean {
        val trimmed = newKey.trim()
        if (trimmed.isEmpty()) return false
        val current = _apiKeys.value.toMutableList()
        if (!current.contains(trimmed)) {
            current.add(trimmed)
            saveKeys(current)
            return true
        }
        return false
    }

    fun removeKey(index: Int) {
        val current = _apiKeys.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            saveKeys(current)
            if (_activeKeyIndex.value >= current.size) {
                _activeKeyIndex.value = (current.size - 1).coerceAtLeast(0)
                prefs.edit().putInt("active_key_index", _activeKeyIndex.value).apply()
            }
        }
    }

    fun rotateToNextKeyOnQuotaExhausted(): String? {
        val keys = _apiKeys.value
        if (keys.size <= 1) {
            _lastQuotaExhaustedMessage.value = "Active API key quota exhausted. No substitute keys configured."
            return null
        }
        val nextIndex = (_activeKeyIndex.value + 1) % keys.size
        _activeKeyIndex.value = nextIndex
        prefs.edit().putInt("active_key_index", nextIndex).apply()
        _lastQuotaExhaustedMessage.value = "Quota reached on key #${_activeKeyIndex.value}. Switched to substitute key #${nextIndex + 1}."
        return keys[nextIndex]
    }

    fun clearQuotaMessage() {
        _lastQuotaExhaustedMessage.value = null
    }

    private fun saveKeys(keys: List<String>) {
        _apiKeys.value = keys
        val jsonArray = JSONArray()
        for (k in keys) {
            jsonArray.put(k)
        }
        prefs.edit().putString("backup_keys_json", jsonArray.toString()).apply()
    }
}
