package com.operit.pet

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * 开机自启广播接收器
 * 手机重启后自动拉起桌宠悬浮窗
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val handler = android.os.Handler(android.os.Looper.getMainLooper())
            handler.postDelayed({
                try {
                    val serviceIntent = Intent(context, OverlayService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                } catch (_: Exception) {}
            }, 5000)
        }
    }
}