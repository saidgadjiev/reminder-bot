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

    public Integer getMessageId(long chatId) {
        return jdbcTemplate.query(
                "SELECT message_id FROM payment_message WHERE chat_id = ?",
                ps -> ps.setLong(1, chatId),
                rs -> {
                    if (rs.next()) {
                        return rs.getInt("message_id");
                    }

                    return null;
                }
        );
    }

    public void create(long chatId, int messageId) {
        jdbcTemplate.update(
                "INSERT INTO payment_message(chat_id, message_id) VALUES (" + chatId + ", " + messageId + ") " +
                        "ON CONFLICT (chat_id) DO UPDATE SET message_id = excluded.message_id"
        );
    }

    public Integer delete(long chatId) {
        return jdbcTemplate.query(
                "WITH pm AS (DELETE FROM payment_message WHERE chat_id = ? RETURNING message_id) SELECT * FROM pm",
                ps -> ps.setLong(1, chatId),
                rs -> {
                    if (rs.next()) {
                        return rs.getInt("message_id");
                    }

                    return null;
                }
        );
    }
}
