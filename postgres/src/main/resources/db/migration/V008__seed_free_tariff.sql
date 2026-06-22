ALTER TABLE tariffs ADD CONSTRAINT IF NOT EXISTS uq_tariffs_name UNIQUE (name);

INSERT INTO tariffs (id, name, max_projects, max_boards_per_project, max_tasks_per_board, max_file_size_mb, max_storage_mb, created_at)
VALUES ('00000000-0000-0000-0000-000000000001', 'Free', 5, 3, 50, 10, 100, NOW())
ON CONFLICT (name) DO NOTHING;
