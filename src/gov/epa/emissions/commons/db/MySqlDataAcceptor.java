package gov.epa.emissions.commons.db;

import java.sql.Connection;
import java.sql.Statement;

/**
 * An acceptor which takes in data and puts it in a database
 */

public class MySqlDataAcceptor extends AbstractDataAcceptor {

    public MySqlDataAcceptor(Connection connection, boolean useTransactions, boolean usePrepStatement) {
        super(connection);
    }

    public String customizeCreateTableQuery(String origQueryString) {
        String queryString = origQueryString;
        // TODO: do we need InnoDB ? If so, when ?
        // if (useTransactions) {
        // queryString += " type=InnoDB";
        // }
        return queryString;
    }

    public void insertRow(String table, String[] data, String[] colTypes) throws Exception {
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

    public void addColumn(String table, String columnName, String columnType, String afterColumnName) throws Exception {
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
