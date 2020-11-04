ALTER TABLE reminder ADD time_tracker BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE reminder ADD last_work_in_progress_at TIMESTAMP(0);
ALTER TABLE reminder ADD elapsed_time INTERVAL;
ALTER TABLE reminder ADD estimate INTERVAL;

ALTER TABLE completed_reminder ADD time_tracker BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE completed_reminder ADD estimate INTERVAL;
ALTER TABLE completed_reminder ADD elapsed_time INTERVAL;