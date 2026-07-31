package com.operit.pet

/**
 * 应用配置
 */
object Config {
    // Supabase 连接信息
    const val SUPABASE_URL = "https://ekfxdoewooienrcpngtr.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVrZnhkb2V3b29laW5yY3BuZ3RrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODU0NTkxNTcsImV4cCI6MjEwMTAzNTE1N30.S4EoKEnMDVIf6EwEUXKwGVln6jwxd35j7_JUksYA3uE"

    // 桌宠显示尺寸（dp）
    const val PET_WIDTH = 180
    const val PET_HEIGHT = 240

    // 互动参数
    const val DOUBLE_TAP_TIMEOUT = 300L       // 毫秒
    const val LONG_PRESS_TIMEOUT = 600L       // 毫秒
    const val MOVE_THRESHOLD = 10             // dp
    const val WHISPER_INTERVAL = 3600_000L    // 通知碎碎念间隔（1小时）

    // 感知系统
    const val APP_POLL_INTERVAL = 3000L       // 前台app检测间隔（3秒）
    const val SUPABASE_POLL_INTERVAL = 5000L  // 轮询AI状态间隔（5秒）
}
