package ru.gadjini.reminder.dao.subscription;

import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.gadjini.reminder.domain.Subscription;
import ru.gadjini.reminder.service.jdbc.ResultSetMapper;
import ru.gadjini.reminder.util.JodaTimeUtils;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Map;

@Repository
@Qualifier("db")
public class DbSubscriptionDao implements SubscriptionDao {

    private JdbcTemplate jdbcTemplate;

    private ResultSetMapper resultSetMapper;

    @Autowired
    public DbSubscriptionDao(JdbcTemplate jdbcTemplate, ResultSetMapper resultSetMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.resultSetMapper = resultSetMapper;
    }

    @Override
    public Subscription getSubscription(int userId) {
        return jdbcTemplate.query(
                "SELECT * FROM subscription WHERE user_id = ?",
                ps -> ps.setInt(1, userId),
                rs -> {
                    if (rs.next()) {
                        return resultSetMapper.mapSubscription(rs);
                    }

                    return null;
                }
        );
    }

    @Override
    public void create(Subscription subscription) {
        new SimpleJdbcInsert(jdbcTemplate)
                .withTableName(Subscription.TABLE)
                .execute(sqlParameterSource(subscription));
    }

    @Override
    public LocalDate update(Period period, int planId, int userId) {
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                con -> {
                    PreparedStatement ps = con.prepareStatement("UPDATE subscription SET end_date = end_date + ?, plan_id = ? WHERE user_id = ? RETURNING end_date", Statement.RETURN_GENERATED_KEYS);

                    ps.setObject(1, JodaTimeUtils.toPgInterval(period));
                    ps.setInt(2, planId);
                    ps.setInt(3, userId);

                    return ps;
                },
                generatedKeyHolder
        );

        Map<String, Object> keys = generatedKeyHolder.getKeys();
        Date endDate = (Date) keys.get(Subscription.END_DATE);

        return endDate.toLocalDate();
    }

    private SqlParameterSource sqlParameterSource(Subscription subscription) {
        return new MapSqlParameterSource()
                .addValue(Subscription.USER_ID, subscription.getUserId())
                .addValue(Subscription.END_DATE, Date.valueOf(subscription.getEndDate()))
                .addValue(Subscription.PLAN_ID, subscription.getPlanId());
    }
}
