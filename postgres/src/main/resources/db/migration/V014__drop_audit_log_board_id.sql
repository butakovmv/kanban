ALTER TABLE audit_log DROP COLUMN IF EXISTS board_id;
DROP INDEX IF EXISTS idx_audit_log_board_id;
