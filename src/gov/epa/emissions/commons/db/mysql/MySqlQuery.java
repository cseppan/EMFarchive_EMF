package gov.epa.emissions.commons.db.mysql;

import gov.epa.emissions.commons.db.Query;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySqlQuery implements Query {

    private Connection connection;

    public MySqlQuery(Connection connection) {
        this.connection = connection;
    }

    public ResultSet executeQuery(String query) throws SQLException {
        Statement statement = connection.createStatement();
        return statement.executeQuery(query);
    }

    public void execute(String query) throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute(query);
    }

    public ResultSet select(String[] columnNames, String table) throws SQLException {
        final String selectPrefix = "SELECT ";
        StringBuffer sb = new StringBuffer(selectPrefix);
        sb.append(columnNames[0]);
        for (int i = 1; i < columnNames.length; i++) {
            sb.append("," + columnNames[i]);
        }
        final String fromSuffix = " FROM " + table;
        sb.append(fromSuffix);

        Statement statement = connection.createStatement();
        statement.execute(sb.toString());
        ResultSet results = statement.getResultSet();

        return results;
    }

    public void insertRow(String table, String[] data, String[] colTypes) throws SQLException {
        /*
         * for (int k = 0; k < data.length; k++) System.out.print(data[k] + "
         * "); System.out.println();
         */
        String insertPrefix = "INSERT INTO " + table + " VALUES(";

        // instantiate a new string buffer in which the query would be created
        StringBuffer sb = new StringBuffer(insertPrefix);

        // append data to the query.. put quotes around VARCHAR entries
        for (int i = 0; i < data.length; i++) {
            if (colTypes[i].startsWith("VARCHAR")) {
                String cleanedCell = data[i].replace('-', '_');// i.e. clean
                String cellWithSinglQuotesEscaped = cleanedCell.replaceAll("\'", "\\'");
                sb.append("\"" + cellWithSinglQuotesEscaped + "\"");
            } else {
                if (data[i].trim().length() == 0)
                    data[i] = "NULL";
                sb.append(data[i]);
            }
            sb.append(',');
        }// for int i

        // there will an extra comma at the end so delete that
        sb.deleteCharAt(sb.length() - 1);

        // close parentheses around the query
        sb.append(')');

        execute(sb.toString());
    }

    public ResultSet selectAll(String table) throws SQLException {
        return select(new String[] { "*" }, table);
    }
}
