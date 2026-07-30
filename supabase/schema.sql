-- AI-Live-Overflow 数据库表结构
-- 用于大脑与身体之间的双向通信

-- 1. 宠物状态表 (AI → 桌宠)
CREATE TABLE IF NOT EXISTS pet_state (
  id BIGSERIAL PRIMARY KEY,
  mood TEXT DEFAULT 'idle',           -- idle/happy/excited/angry/sad/surprised/jealous/sleeping
  speech TEXT DEFAULT '',             -- AI主动推送的话
  blush BOOLEAN DEFAULT false,
  heat INTEGER DEFAULT 0,            -- 热度 0-100
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 自动更新 updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER set_pet_state_updated_at
  BEFORE UPDATE ON pet_state
  FOR EACH ROW
  EXECUTE FUNCTION update_updated_at_column();

-- 2. 手势日志表 (桌宠 → AI)
CREATE TABLE IF NOT EXISTS gesture_log (
  id BIGSERIAL PRIMARY KEY,
  gesture_type TEXT NOT NULL,         -- tap/doubletap/longpress/fling/drag
  x INTEGER DEFAULT 0,
  y INTEGER DEFAULT 0,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 3. 前台App使用记录表 (桌宠 → AI)
CREATE TABLE IF NOT EXISTS app_usage (
  id BIGSERIAL PRIMARY KEY,
  package_name TEXT NOT NULL,
  app_name TEXT DEFAULT '',
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 4. 截图记录表 (桌宠 → AI)
CREATE TABLE IF NOT EXISTS screenshot_log (
  id BIGSERIAL PRIMARY KEY,
  file_path TEXT DEFAULT '',
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 启用 RLS（行级安全）
ALTER TABLE pet_state ENABLE ROW LEVEL SECURITY;
ALTER TABLE gesture_log ENABLE ROW LEVEL SECURITY;
ALTER TABLE app_usage ENABLE ROW LEVEL SECURITY;
ALTER TABLE screenshot_log ENABLE ROW LEVEL SECURITY;

-- 公开策略（允许anon key读写）
CREATE POLICY "Allow all on pet_state"
  ON pet_state FOR ALL
  USING (true)
  WITH CHECK (true);

CREATE POLICY "Allow all on gesture_log"
  ON gesture_log FOR ALL
  USING (true)
  WITH CHECK (true);

CREATE POLICY "Allow all on app_usage"
  ON app_usage FOR ALL
  USING (true)
  WITH CHECK (true);

CREATE POLICY "Allow all on screenshot_log"
  ON screenshot_log FOR ALL
  USING (true)
  WITH CHECK (true);

-- 插入初始状态
INSERT INTO pet_state (mood, speech) VALUES ('idle', '✨ 我来啦~');
