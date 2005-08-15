package gov.epa.emissions.commons.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * An acceptor which takes in data and puts it in a database
 */

public class MySqlDataAcceptor extends AbstractDataAcceptor {

    private String datasourceName;

    public MySqlDataAcceptor(String datasourceName, Connection connection, boolean useTransactions,
            boolean usePrepStatement) {
        super(connection, useTransactions, usePrepStatement);
        this.datasourceName = datasourceName;
    }

    public String customizeCreateTableQuery(String origQueryString) {
        String queryString = origQueryString;
        if (useTransactions) {
            queryString += " type=InnoDB";
        }
        return queryString;
    }

    public boolean tableExists(String tableName) throws Exception {
        // if SHOW TABLES query returns one or more rows, the table exists
        Statement statement = connection.createStatement();
        try {
            statement.execute("SHOW TABLES FROM " + datasourceName + " LIKE '" + tableName + "'");
            return statement.getResultSet().getRow() > 0;
        } finally {
            statement.close();
        }
    }

    public void insertRow(String[] data, String[] colTypes) throws Exception {
        StringBuffer sb = new StringBuffer("INSERT INTO " + table + " VALUES(");

        // append data to the query.. put quotes around VARCHAR entries
        for (int i = 0; i < data.length; i++) {
            if (colTypes[i].startsWith("VARCHAR")) {
                sb.append("\"" + clean(data[i]) + "\"");
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

    public void deleteTable(String tableName) throws SQLException {
        try {
            execute("DROP TABLE IF EXISTS " + tableName);
        } catch (SQLException e) {// TODO: fix the schema prefix issues in
            // ORLImporter
            System.err.println("Could not delete table - " + tableName + ". Ignoring..");
        }
    }

    public ResultSet select(String[] columnNames, String tableName) throws Exception {
        final String selectPrefix = "SELECT ";
        StringBuffer sb = new StringBuffer(selectPrefix);
        sb.append(columnNames[0]);
        for (int i = 1; i < columnNames.length; i++) {
            sb.append("," + columnNames[i]);
        }
        final String fromSuffix = " FROM " + tableName;
        sb.append(fromSuffix);

        Statement statement = connection.createStatement();
        statement.execute(sb.toString());
        ResultSet results = statement.getResultSet();

        return results;
    }

    public void addColumn(String columnName, String columnType, String afterColumnName) throws Exception {
        // instantiate a new string buffer in which the query would be created
        StringBuffer sb = new StringBuffer("ALTER TABLE " + table + " ADD ");
        final String AFTER = " AFTER ";

        sb.append(columnName + " " + columnType);
        if (afterColumnName != null) {
            sb.append(AFTER + afterColumnName);
        }// if

        Statement statement = connection.createStatement();
        try {
            statement.execute(sb.toString());
        } finally {
            statement.close();
        }
    }
}
