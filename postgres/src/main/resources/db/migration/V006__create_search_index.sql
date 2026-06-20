-- Add tsvector column for full-text search
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS search_vector tsvector;

-- Create GIN index
CREATE INDEX IF NOT EXISTS idx_tasks_search_vector ON tasks USING GIN(search_vector);

-- Create trigger function to update tsvector
CREATE OR REPLACE FUNCTION update_task_search_vector() RETURNS trigger AS $$
BEGIN
    NEW.search_vector := to_tsvector('russian', COALESCE(NEW.title, '') || ' ' || COALESCE(NEW.description, ''));
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger
DROP TRIGGER IF EXISTS trg_tasks_search_vector ON tasks;
CREATE TRIGGER trg_tasks_search_vector
    BEFORE INSERT OR UPDATE ON tasks
    FOR EACH ROW EXECUTE FUNCTION update_task_search_vector();

-- Update existing rows
UPDATE tasks SET search_vector = to_tsvector('russian', COALESCE(title, '') || ' ' || COALESCE(description, ''));
