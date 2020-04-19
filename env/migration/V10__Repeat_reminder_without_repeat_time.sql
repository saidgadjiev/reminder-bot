ALTER TABLE reminder ALTER COLUMN remind_at DROP NOT NULL;
ALTER TABLE reminder ALTER COLUMN initial_remind_at DROP NOT NULL;

ALTER TABLE completed_reminder ALTER COLUMN remind_at DROP NOT NULL;
ALTER TABLE completed_reminder ALTER COLUMN initial_remind_at DROP NOT NULL;