package com.operit.pet

import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

/**
 * Supabase 后端同步 — 模块⑥: supabase-sync.md
 * 连接你的 AI 大脑与 Android 桌宠身体
 *
 * 使用前先在 Operit 环境变量中设置:
 *   SUPABASE_URL = https://xxx.supabase.co
 *   SUPABASE_KEY = your-anon-or-service-key
 */
object SupabaseConfig {
    var url: String = ""
    var key: String = ""
}

class SupabaseSync {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * 上报手势事件
     */
    fun logGesture(type: String, x: Int = 0, y: Int = 0) {
        val body = JSONObject().apply {
            put("gesture_type", type)
            put("x", x)
            put("y", y)
        }
        postToSupabase("gesture_log", body)
    }

    /**
     * 上报前台 app 变化
     */
    fun logAppUsage(packageName: String) {
        val body = JSONObject().apply {
            put("package_name", packageName)
        }
        postToSupabase("app_usage", body)
    }

    /**
     * 从后端读取 AI 推送的状态
     * 返回最新一条 pet_state
     */
    fun fetchLatestState(): JSONObject? {
        return try {
            val url = URL("${SupabaseConfig.url}/rest/v1/pet_state?order=updated_at.desc&limit=1")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("apikey", SupabaseConfig.key)
            conn.setRequestProperty("Authorization", "Bearer ${SupabaseConfig.key}")
            val response = conn.inputStream.bufferedReader().readText()
            conn.disconnect()
            val arr = org.json.JSONArray(response)
            if (arr.length() > 0) arr.getJSONObject(0) else null
        } catch (e: Exception) {
            null
        }
    }

    private fun postToSupabase(table: String, body: JSONObject) {
        if (SupabaseConfig.url.isEmpty() || SupabaseConfig.key.isEmpty()) return
        scope.launch {
            try {
                val url = URL("${SupabaseConfig.url}/rest/v1/$table")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("apikey", SupabaseConfig.key)
                conn.setRequestProperty("Authorization", "Bearer ${SupabaseConfig.key}")
                conn.setRequestProperty("Prefer", "return=minimal")
                conn.doOutput = true
                conn.outputStream.use { it.write(body.toString().toByteArray()) }
                conn.responseCode
                conn.disconnect()
            } catch (_: Exception) {}
        }
    }

    fun destroy() {
        scope.cancel()
    }
}
