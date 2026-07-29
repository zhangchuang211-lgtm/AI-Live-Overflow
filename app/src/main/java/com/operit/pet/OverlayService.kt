package com.operit.pet

import android.app.*
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.*
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebSettings
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Operit 悬浮窗核心服务
 * 修复：setLayerType 位置、通知图标兼容、空指针保护
 */
class OverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: WebView? = null
    private var params: WindowManager.LayoutParams? = null

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var lastTapTime = 0L
    private var touchStartTime = 0L
    private var hasMoved = false
    private var tapCount = 0
    private var lastTapWindowStart = 0L
    private var notificationHandler: Handler? = null

    companion object {
        private const val CHANNEL_ID = "operit_pet_channel"
        private const val NOTIFICATION_ID = 1001
        private const val PET_SIZE_DP = 180
        private const val PET_HEIGHT_DP = 240
        private const val DOUBLE_TAP_TIMEOUT = 300L
        private const val LONG_PRESS_TIMEOUT = 600L
        private const val MOVE_THRESHOLD = 10
        private const val WHISPER_INTERVAL = 3600_000L
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        try {
            createNotificationChannel()
            val notification = buildNotification("🦊 Operit 来啦~")
            startForeground(NOTIFICATION_ID, notification)

            setupOverlay()
            startWhisperRotation()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupOverlay() {
        try {
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

            params = WindowManager.LayoutParams(
                dpToPx(PET_SIZE_DP),
                dpToPx(PET_HEIGHT_DP),
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 50
                y = 300
            }

            overlayView = WebView(this).apply {
                setBackgroundColor(0x00000000)
                // ✅ 正确位置：setLayerType 是 View 的方法，在 WebView 自身上调用
                setLayerType(View.LAYER_TYPE_HARDWARE, null)
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    allowFileAccess = true
                    cacheMode = WebSettings.LOAD_DEFAULT
                }
                webViewClient = WebViewClient()
                loadUrl("file:///android_asset/pet.html")
                setOnTouchListener(createTouchListener())
            }

            windowManager?.addView(overlayView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createTouchListener(): View.OnTouchListener {
        return View.OnTouchListener { _, event ->
            try {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params?.x ?: 0
                        initialY = params?.y ?: 0
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        touchStartTime = System.currentTimeMillis()
                        hasMoved = false
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - initialTouchX).toInt()
                        val dy = (event.rawY - initialTouchY).toInt()
                        if (abs(dx) > MOVE_THRESHOLD || abs(dy) > MOVE_THRESHOLD) {
                            hasMoved = true
                            params?.x = initialX + dx
                            params?.y = initialY + dy
                            windowManager?.updateViewLayout(overlayView, params)
                        }
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        val elapsed = System.currentTimeMillis() - touchStartTime
                        if (!hasMoved) {
                            when {
                                elapsed > LONG_PRESS_TIMEOUT -> onLongPress()
                                System.currentTimeMillis() - lastTapTime < DOUBLE_TAP_TIMEOUT -> onDoubleTap()
                                else -> {
                                    lastTapTime = System.currentTimeMillis()
                                    onTap()
                                }
                            }
                        } else {
                            val dx = (event.rawX - initialTouchX).toInt()
                            val dy = (event.rawY - initialTouchY).toInt()
                            val velocity = sqrt((dx * dx + dy * dy).toDouble())
                            if (velocity > 200 && elapsed < 400) onFling(dx, dy)
                            else onDragEnd()
                        }
                        true
                    }
                    else -> false
                }
            } catch (e: Exception) {
                false
            }
        }
    }

    private fun onTap() {
        overlayView?.evaluateJavascript(
            "window.petEngine && window.petEngine.onTap()", null
        )
    }

    private fun onDoubleTap() {
        overlayView?.evaluateJavascript(
            "window.petEngine && window.petEngine.onDoubleTap()", null
        )
    }

    private fun onLongPress() {
        overlayView?.evaluateJavascript(
            "window.petEngine && window.petEngine.onLongPress()", null
        )
    }

    private fun onFling(dx: Int, dy: Int) {
        overlayView?.evaluateJavascript(
            "window.petEngine && window.petEngine.onFling($dx, $dy)", null
        )
    }

    private fun onDragEnd() {
        overlayView?.evaluateJavascript(
            "window.petEngine && window.petEngine.onDragEnd()", null
        )
    }

    // ========== 通知 ==========

    private fun startWhisperRotation() {
        try {
            notificationHandler = Handler(Looper.getMainLooper())
            notificationHandler?.postDelayed(object : Runnable {
                override fun run() {
                    try {
                        updateWhisper()
                        notificationHandler?.postDelayed(this, WHISPER_INTERVAL)
                    } catch (e: Exception) {}
                }
            }, WHISPER_INTERVAL)
        } catch (e: Exception) {}
    }

    private fun updateWhisper() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIFICATION_ID, buildNotification(getWhisper()))
    }

    private fun getWhisper(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when {
            hour in 0..5 -> "🌙 这么晚了还不睡..."
            hour in 6..8 -> "☀️ 早上好呀！"
            hour in 12..13 -> "🍚 记得吃饭！"
            else -> "🦊 我在呢~"
        }
    }

    private fun buildNotification(text: String): Notification {
        // 使用原生 API，避免 NotificationCompat 兼容问题
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) CHANNEL_ID else ""
        // 用 android.R.drawable.ic_dialog_info 替代 ic_menu_compass（更安全）
        return Notification.Builder(this)
            .setContentTitle("🦊 Operit")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    setChannelId(channelId)
                }
            }
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Operit 桌宠",
                    NotificationManager.IMPORTANCE_LOW
                ).apply { setShowBadge(false) }
                getSystemService(NotificationManager::class.java)
                    .createNotificationChannel(channel)
            } catch (e: Exception) {}
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onDestroy() {
        try {
            overlayView?.let {
                windowManager?.removeView(it)
                it.destroy()
            }
        } catch (e: Exception) {}
        overlayView = null
        notificationHandler?.removeCallbacksAndMessages(null)
        try { stopForeground(STOP_FOREGROUND_REMOVE) } catch (e: Exception) {}
        super.onDestroy()
    }
}
