package ru.gadjini.reminder.jdbc;

import org.apache.commons.lang3.ArrayUtils;
import org.jooq.*;
import org.jooq.tools.StringUtils;
import org.jooq.util.postgres.PostgresUtils;
import org.springframework.jdbc.core.ArgumentTypePreparedStatementSetter;
import org.springframework.jdbc.core.ParameterDisposer;
import org.springframework.jdbc.core.PreparedStatementSetter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

import static java.lang.Integer.toOctalString;
import static org.jooq.tools.StringUtils.leftPad;

public class JooqPreparedSetter implements PreparedStatementSetter, ParameterDisposer {

    private Object[] values;

    private int[] types;

    private List<Field<?>> fields;

    private ArgumentTypePreparedStatementSetter statementSetter;

    public JooqPreparedSetter(Map<String, Param<?>> params) {
        this(Collections.emptyList(), params);
    }

    public JooqPreparedSetter(Collection<Field<?>> fields, Map<String, Param<?>> params) {
        this.fields = new ArrayList<>(fields);

        List<Object> valuesList = new ArrayList<>();
        List<Integer> typesList = new ArrayList<>();

        for (Param<?> param : params.values()) {
            Object value = param.getValue();
            int type = param.getDataType().getSQLType();
            if (type == Types.ARRAY) {
                value = toPGString(value);
                type = Types.OTHER;
            }
            valuesList.add(value);
            typesList.add(type);
        }

        values = valuesList.toArray();
        types = typesList.stream().mapToInt(i -> i).toArray();
        statementSetter = new ArgumentTypePreparedStatementSetter(values, types);
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        statementSetter.setValues(ps);
    }

    @Override
    public void cleanupParameters() {
        statementSetter.cleanupParameters();
    }

    private static String toPGArrayString(Object[] value) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        String separator = "";
        for (Object o : value) {
            sb.append(separator);

            // [#753] null must be set as a literal
            if (o == null)
                sb.append((Object) null);
            else
                sb.append("\"")
                        .append(StringUtils.replace(StringUtils.replace(toPGString(o), "\\", "\\\\"), "\"", "\\\""))
                        .append("\"");

            separator = ",";
        }

        sb.append("}");
        return sb.toString();
    }

    private static String toPGString(Object o) {
        if (o instanceof byte[])
            return toPGString((byte[]) o);
        else if (o instanceof Object[])
            return toPGArrayString((Object[]) o);
        else if (o instanceof Record)
            return toPGString((Record) o);
        else if (o instanceof EnumType)
            return ((EnumType) o).getLiteral();
        else
            return "" + o;
    }

    private static String toPGString(Record r) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");

        String separator = "";
        for (int i = 0; i < r.size(); i++) {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            Object a = ((Converter) r.field(i).getConverter()).to(r.get(i));
            sb.append(separator);

            // [#753] null must not be set as a literal
            if (a != null) {
                if (a instanceof byte[])
                    sb.append(toPGString((byte[]) a));
                else
                    sb.append(StringUtils.replace(StringUtils.replace(toPGString(a), "\\", "\\\\"), "\"", "\\\""));
            }

            separator = ",";
        }

        sb.append(")");
        return sb.toString();
    }

    private static String toPGString(byte[] binary) {
        StringBuilder sb = new StringBuilder();

        for (byte b : binary) {

            // [#3924] Beware of signed vs unsigned bytes!
            sb.append("\\\\");
            sb.append(leftPad(toOctalString(b & 0x000000ff), 3, '0'));
        }

        return sb.toString();
    }
}
