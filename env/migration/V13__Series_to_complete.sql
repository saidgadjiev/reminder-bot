ALTER TYPE repeat_time ADD ATTRIBUTE rt_series_to_complete INT;
ALTER TABLE reminder ADD COLUMN curr_series_to_complete INT;

