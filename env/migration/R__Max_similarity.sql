CREATE OR REPLACE FUNCTION max_similarity(VARCHAR, VARCHAR[])
    RETURNS max_similarity
AS
$$
DECLARE
    result max_similarity;
    val    VARCHAR;
    sim    REAL;
BEGIN
    result.max_sim = 0;
    FOREACH val IN ARRAY $2
        LOOP
            sim = similarity($1, val);

            if sim > result.max_sim THEN
                result.max_sim = sim;
                result.match_word = val;
            end if;
        end loop;

    RETURN result;
END;
$$ LANGUAGE PLPGSQL;