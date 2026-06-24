ALTER TABLE tasks DROP CONSTRAINT IF EXISTS tasks_board_id_fkey;
ALTER TABLE columns DROP CONSTRAINT IF EXISTS columns_board_id_fkey;

ALTER TABLE tasks ADD COLUMN project_id UUID;
ALTER TABLE columns ADD COLUMN project_id UUID;

UPDATE tasks SET project_id = boards.project_id FROM boards WHERE tasks.board_id = boards.id;
UPDATE columns SET project_id = boards.project_id FROM boards WHERE columns.board_id = boards.id;

ALTER TABLE tasks DROP COLUMN board_id;
ALTER TABLE columns DROP COLUMN board_id;

DROP INDEX IF EXISTS idx_tasks_board_id;
DROP INDEX IF EXISTS idx_columns_board_id;
