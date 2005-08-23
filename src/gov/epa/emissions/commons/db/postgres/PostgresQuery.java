package gov.epa.emissions.commons.db.postgres;

import gov.epa.emissions.commons.db.Query;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PostgresQuery implements Query {

    private Connection connection;

    public PostgresQuery(Connection connection) {
        this.connection = connection;
    }

    public ResultSet executeQuery(String query) throws SQLException {
        Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        return statement.executeQuery(query);
    }

    public void execute(String query) throws SQLException {
        Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        statement.execute(query);
    }

    // FIXME: duplicate methods in both datasources
    public ResultSet select(String[] columnNames, String tableName) throws SQLException {
        final String selectPrefix = "SELECT ";
        StringBuffer query = new StringBuffer(selectPrefix);
        query.append(columnNames[0]);
        for (int i = 1; i < columnNames.length; i++) {
            query.append("," + columnNames[i]);
        }
        final String fromSuffix = " FROM " + tableName;
        query.append(fromSuffix);

        Statement statement = connection.createStatement();
        statement.execute(query.toString());

        return statement.getResultSet();
    }

    public void insertRow(String table, String[] data, String[] colTypes) throws SQLException {
        StringBuffer query = new StringBuffer();
        query.append("INSERT INTO " + table + " VALUES(");

        for (int i = 0; i < data.length; i++) {
            if (colTypes[i].startsWith("VARCHAR")) {
                String cleanedCell = data[i].replace('-', '_');
                String cellWithSinglQuotesEscaped = cleanedCell.replace('\'', ' ');
                query.append("'" + cellWithSinglQuotesEscaped + "'");
            } else {
                if (data[i].trim().length() == 0)
                    data[i] = "NULL";
                query.append(data[i]);
            }
            if (i < (data.length - 1))
                query.append(',');
        }
        query.append(')');// close parentheses around the query

        execute(query.toString());
    }

    public ResultSet selectAll(String table) throws SQLException {
        return select(new String[] { "*" }, table);
    }

}
