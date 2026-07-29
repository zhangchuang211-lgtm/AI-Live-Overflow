package com.operit.pet

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

/**
 * 权限引导页 — 让用户开启悬浮窗、使用情况访问、通知等权限
 */
class MainActivity : AppCompatActivity() {

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { checkAllPermissions() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val statusText = findViewById<TextView>(R.id.statusText)
        val btnOverlay = findViewById<Button>(R.id.btnOverlay)
        val btnUsage = findViewById<Button>(R.id.btnUsage)
        val btnBattery = findViewById<Button>(R.id.btnBattery)
        val btnStart = findViewById<Button>(R.id.btnStart)
        val btnNotification = findViewById<Button>(R.id.btnNotification)

        // 悬浮窗权限
        btnOverlay.setOnClickListener {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            overlayPermissionLauncher.launch(intent)
        }

        // 使用情况访问权限
        btnUsage.setOnClickListener {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            startActivity(intent)
        }

        // 电池白名单 (OPPO 等国产 ROM 需要)
        btnBattery.setOnClickListener {
            try {
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "请手动在设置 > 电池 > 优化中关闭优化", Toast.LENGTH_LONG).show()
            }
        }

        // 通知权限 (Android 13+)
        btnNotification.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Android 13+ 需要手动开启通知权限", Toast.LENGTH_SHORT).show()
            }
        }

        // 启动桌宠！
        btnStart.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "请先开启悬浮窗权限 🦊", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val intent = Intent(this, OverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            Toast.makeText(this, "Operit 来啦！✨", Toast.LENGTH_SHORT).show()
            finish()
        }

        checkAllPermissions()
    }

    override fun onResume() {
        super.onResume()
        checkAllPermissions()
    }

    private fun checkAllPermissions() {
        val statusText = findViewById<TextView>(R.id.statusText)
        val btnStart = findViewById<Button>(R.id.btnStart)

        val canOverlay = Settings.canDrawOverlays(this)
        val canStart = canOverlay

        statusText.text = buildString {
            append("🦊 权限状态：\n")
            appendLine(if (canOverlay) "✅ 悬浮窗权限：已开启" else "❌ 悬浮窗权限：未开启")
        }

        btnStart.isEnabled = canStart
        btnStart.text = if (canStart) "✨ 启动 Operit 桌宠！" else "⚠️ 请先开启悬浮窗权限"
    }
}
