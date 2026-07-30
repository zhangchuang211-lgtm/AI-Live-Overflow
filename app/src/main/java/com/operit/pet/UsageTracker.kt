package com.operit.pet

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import java.util.Timer
import java.util.TimerTask

/**
 * 前台 App 检测 — 模块③: app-detection.md
 */
class UsageTracker(private val context: Context) {

    private var timer: Timer? = null
    private var lastApp: String = ""
    var onAppChanged: ((String) -> Unit)? = null
    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())

    fun start() {
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val current = getForegroundApp()
                if (current != lastApp && current.isNotEmpty()) {
                    lastApp = current
                    // 切到主线程回调，避免 WebView 操作崩溃
                    mainHandler.post { onAppChanged?.invoke(current) }
                }
            }
        }, 0, 3000)
    }

    private fun getForegroundApp(): String {
        try {
            val usm = context.getSystemService(Context.USAGE_STATS_SERVICE)
                    as UsageStatsManager
            val now = System.currentTimeMillis()
            val events = usm.queryEvents(now - 5000, now)
            val event = UsageEvents.Event()
            var foreground = ""
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    foreground = event.packageName
                }
            }
            return foreground
        } catch (e: Exception) {
            return ""
        }
    }

    fun stop() {
        timer?.cancel()
        timer = null
    }
}