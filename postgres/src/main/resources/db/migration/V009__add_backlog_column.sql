-- Add a "Backlog" column to every existing board that doesn't already have one.
-- Backlog column gets position = 0, all existing columns are shifted by +1.

DO $$
DECLARE
    b RECORD;
    new_col_id UUID;
BEGIN
    FOR b IN
        SELECT id FROM boards
        WHERE id NOT IN (
            SELECT board_id FROM columns WHERE name = 'Backlog'
        )
    LOOP
        -- Shift existing columns' positions by 1
        UPDATE columns SET position = position + 1 WHERE board_id = b.id;

        -- Insert Backlog column at position 0
        INSERT INTO columns (id, board_id, name, position, wip_limit, created_at)
        VALUES (gen_random_uuid(), b.id, 'Backlog', 0, NULL, NOW());
    END LOOP;
END $$;
