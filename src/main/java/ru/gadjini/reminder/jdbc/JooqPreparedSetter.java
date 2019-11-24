package ru.gadjini.reminder.jdbc;

import org.checkerframework.checker.units.qual.A;
import org.jooq.Param;
import org.springframework.jdbc.core.ArgumentTypePreparedStatementSetter;
import org.springframework.jdbc.core.ParameterDisposer;
import org.springframework.jdbc.core.PreparedStatementSetter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JooqPreparedSetter implements PreparedStatementSetter, ParameterDisposer {

    private ArgumentTypePreparedStatementSetter statementSetter;

    public JooqPreparedSetter(Map<String, Param<?>> params) {
        List<Object> values = new ArrayList<>();
        List<Integer> types = new ArrayList<>();

        for (Param<?> param: params.values()) {
            values.add(param.getValue());
            types.add(param.getDataType().getSQLType());
        }

        statementSetter = new ArgumentTypePreparedStatementSetter(values.toArray(), types.stream().mapToInt(i -> i).toArray());
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        statementSetter.setValues(ps);
    }

    @Override
    public void cleanupParameters() {
        statementSetter.cleanupParameters();
    }
}
