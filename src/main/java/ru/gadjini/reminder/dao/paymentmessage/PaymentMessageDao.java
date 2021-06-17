package ru.gadjini.reminder.dao.paymentmessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentMessageDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public PaymentMessageDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Integer getMessageId(long userId) {
        return jdbcTemplate.query(
                "SELECT message_id FROM payment_message WHERE user_id = ?",
                ps -> ps.setLong(1, userId),
                rs -> {
                    if (rs.next()) {
                        return rs.getInt("message_id");
                    }

                    return null;
                }
        );
    }

    public void create(long userId, int messageId) {
        jdbcTemplate.update(
                "INSERT INTO payment_message(user_id, message_id) VALUES (" + userId + ", " + messageId + ") " +
                        "ON CONFLICT (user_id) DO UPDATE SET message_id = excluded.message_id"
        );
    }

    public Integer delete(long userId) {
        return jdbcTemplate.query(
                "WITH pm AS (DELETE FROM payment_message WHERE user_id = ? RETURNING message_id) SELECT * FROM pm",
                ps -> ps.setLong(1, userId),
                rs -> {
                    if (rs.next()) {
                        return rs.getInt("message_id");
                    }

                    return null;
                }
        );
    }
}
