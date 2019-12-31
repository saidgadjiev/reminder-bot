CREATE OR REPLACE FUNCTION max_similarity(VARCHAR, VARCHAR[]) RETURNS REAL AS
$$
DECLARE
    max FLOAT;
    val VARCHAR;
    sim FLOAT;
BEGIN
    max = 0;
    FOREACH val IN ARRAY $2
        LOOP
            sim = similarity($1, val);

            if sim > max THEN
                max = sim;
            end if;
        end loop;

    RETURN max;
END;
$$ LANGUAGE PLPGSQL