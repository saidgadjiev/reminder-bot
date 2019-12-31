CREATE TYPE max_similarity AS (
    match_word VARCHAR,
    max_sim REAL
    );

CREATE TYPE datetime AS (
    dt_date date,
    dt_time time
    );

CREATE TYPE day_of_week AS ENUM ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY');

CREATE TYPE month AS ENUM ('JANUARY', 'FEBRUARY', 'MARCH', 'APRIL', 'MAY', 'JUNE', 'JULY', 'AUGUST', 'SEPTEMBER', 'OCTOBER', 'NOVEMBER', 'DECEMBER');

CREATE TYPE repeat_time AS (
    rt_day_of_week day_of_week,
    rt_time time,
    rt_interval INTERVAL,
    rt_month month,
    rt_day int
    );
