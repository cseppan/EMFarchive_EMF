package gov.epa.emissions.commons.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PostgresDatasource implements Datasource {

    private Connection connection;

    private PostgresDataAcceptor dataAcceptor;

    private String name;

   
    public PostgresDatasource(String name, Connection connection) {
        this.connection = connection;
        this.name = name;
        this.dataAcceptor = new PostgresDataAcceptor(connection, false, true);
    }

    public String getName() {
        return name;
    }

    public Connection getConnection() {
        return connection;
    }

    public List getTableNames() throws SQLException {
        List tableNames = new ArrayList();

        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet tables = metaData.getTables(null, null, null, new String[] { "TABLE" });
        while (tables.next()) {
            String tableName = tables.getString("TABLE_NAME");
            tableNames.add(tableName);
        }

        return tableNames;
    }

    /**
     * Execute an SQL query and return the result set.
     */
    public ResultSet executeQuery(final String query) throws SQLException {
        Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        return statement.executeQuery(query);
    }

    public void execute(final String query) throws SQLException {
        Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        statement.execute(query);
    }

    /**
     * Create the table using the header with multiple primary colums NOTE:
     * please ensure that primaryCols is a subset of colNames before calling
     * this method
     */
    public void createTable(String tableName, String[] colNames, String[] colTypes, String[] primaryCols,
            boolean overwrite) throws SQLException {
        int length = colNames.length;
        if (length != colTypes.length)
            throw new SQLException("There are different numbers of column names and types");

        if (overwrite) {
            try {
                execute("DROP TABLE " + name + "." + tableName);
            } catch (Exception e) {
                // TODO: ignore (for postgress)
            }
        }

        String queryString = "CREATE TABLE " + getName() + "." + tableName + " (";

        for (int i = 0; i < length - 1; i++) {
            queryString += clean(colNames[i]) + " " + colTypes[i] + ", ";
        }// for i
        queryString += clean(colNames[length - 1]) + " " + colTypes[length - 1];

        String primaryColumns = "";
        if (primaryCols != null && primaryCols.length != 0) {
            primaryColumns = ", PRIMARY KEY (";
            for (int i = 0; i < primaryCols.length - 1; i++) {
                primaryColumns += clean(primaryCols[i]) + ", ";
            }
            primaryColumns += clean(primaryCols[primaryCols.length - 1]) + " )";
        }
        queryString = queryString + primaryColumns + ")";

        execute(queryString);
    }

    private String clean(String data) {
        return (data).replace('-', '_');
    }

    public void insertRow(String table, String[] data, String[] colTypes) throws SQLException {
        StringBuffer query = new StringBuffer();
        query.append("INSERT INTO " + getName() + "." + table + " VALUES(");

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

    public AbstractDataAcceptor getDataAcceptor() {
        return dataAcceptor;
    }

}
