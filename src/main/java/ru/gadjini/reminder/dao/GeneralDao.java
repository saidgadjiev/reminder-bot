package ru.gadjini.reminder.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class GeneralDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public GeneralDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Object getAttr(String table, String attr, String condition) {
        return jdbcTemplate.query(
                "SELECT " + attr + " FROM " + table + " WHERE " + condition,
                new ResultSetExtractor<Object>() {
                    @Override
                    public Object extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                        if (resultSet.next()) {
                            return resultSet.getObject(attr);
                        }

                        return null;
                    }
                }
        );
    }
}
