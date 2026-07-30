package com.operit.pet

/**
 * 应用配置
 */
object Config {
    // Supabase 连接信息
    const val SUPABASE_URL = "https://fcoxjcukoglfgcwioeie.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZjb3hqY3Vrb2dsZmdjaW9laWUiLCJyb2xlIjoiYW5vbiIsImlhdCI6MTc4NTQ0MzExNSwiZXhwIjoyMTAxMDE5MTE1fQ.u5yxR3VEgEV8lKc8tCS1GJrHJvoeym9RaLISNsXJWNA"

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
