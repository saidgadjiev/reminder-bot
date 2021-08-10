ALTER TABLE goal ADD COLUMN goal_id INT REFERENCES goal(id) ON DELETE CASCADE;

alter table goal add column completed BOOLEAN NOT NULL DEFAULT FALSE;