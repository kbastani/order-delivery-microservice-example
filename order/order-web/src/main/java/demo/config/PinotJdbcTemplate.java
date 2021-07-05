package demo.config;

import org.apache.pinot.client.PinotDriver;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PinotJdbcTemplate {

    private final PinotDriver pinotDriver;
    private final String pinotUri;

    public PinotJdbcTemplate(String uri) {
        this.pinotDriver = new PinotDriver();
        this.pinotUri = uri;
        try {
            DriverManager.registerDriver(pinotDriver);

        } catch (Exception ex) {
            throw new RuntimeException("Unable to connect to Pinot with provided URI", ex);
        }
    }

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(pinotUri);
    }

    public List<Map<String, Object>> executeQuery(String query) throws SQLException {
        Connection conn = DriverManager.getConnection(pinotUri);
        List<Map<String, Object>> result;
        ResultSet rs;

        try (Statement statement = conn.createStatement()) {
            rs = statement.executeQuery(query);
            result = resultSetToArrayList(rs);
            conn.close();
        }

        return result;
    }

    private List<Map<String, Object>> resultSetToArrayList(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();
        List<Map<String, Object>> list = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            if (md.getColumnCount() > 0) {
                for (int i = 1; i <= columns; ++i) {
                    Object columnValue = null;
                    switch (md.getColumnTypeName(i)) {
                        case "INT" -> {
                            columnValue = rs.getInt(md.getColumnName(i));
                        }
                        case "FLOAT" -> {
                            columnValue = rs.getFloat(md.getColumnName(i));
                        }
                        case "DOUBLE" -> {
                            columnValue = rs.getDouble(md.getColumnName(i));
                        }
                        case "TIMESTAMP" -> {
                            columnValue = rs.getTimestamp(md.getColumnName(i));
                        }
                        case "BYTES" -> {
                            columnValue = rs.getBytes(md.getColumnName(i));
                        }
                        case "LONG" -> {
                            columnValue = rs.getLong(md.getColumnName(i));
                        }
                        case "STRING" -> {
                            columnValue = rs.getString(md.getColumnName(i));
                        }
                        case "BOOLEAN" -> {
                            columnValue = rs.getBoolean(md.getColumnName(i));
                        }
                        case "JSON" -> {
                            columnValue = rs.getString(md.getColumnName(i));
                        }
                        default -> {
                            columnValue = rs.getString(md.getColumnName(i));
                        }
                    }
                    row.put(md.getColumnName(i), columnValue);
                }
            }
            list.add(row);
        }

        return list;
    }
}
