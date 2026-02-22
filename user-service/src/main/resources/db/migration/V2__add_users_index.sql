-- Additional indexes for performance
ALTER TABLE users ADD INDEX IF NOT EXISTS idx_users_created_at (created_at);
