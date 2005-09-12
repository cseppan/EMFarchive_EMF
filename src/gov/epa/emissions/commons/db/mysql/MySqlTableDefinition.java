package gov.epa.emissions.commons.db.mysql;

import gov.epa.emissions.commons.db.TableDefinition;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MySqlTableDefinition implements TableDefinition {

    private Connection connection;

    private String datasource;

    public MySqlTableDefinition(String datasource, Connection connection) {
        this.datasource = datasource;
        this.connection = connection;
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

    public void createTableWithOverwrite(String tableName, String[] colNames, String[] colTypes, String[] primaryCols)
            throws SQLException {
        // check to see if there are the same number of column names and column
        // types
        int length = colNames.length;
        if (length != colTypes.length)
            throw new SQLException("There are different numbers of column names and types");

        deleteTable(tableName);

        String queryString = "CREATE TABLE " + tableName + " (";

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

    public void createTable(String table, String[] colNames, String[] colTypes, String primaryCol) throws SQLException {
        if (colNames.length != colTypes.length)
            throw new SQLException("There are different numbers of column names and types");

        String ddlStatement = "CREATE TABLE " + table + " (";

        for (int i = 0; i < colNames.length; i++) {
            // one of the columnnames was "dec" for december.. caused a problem
            // there
            if (colNames[i].equals("dec"))
                colNames[i] = colNames[i] + "1";

            ddlStatement = ddlStatement + clean(colNames[i]) + " " + colTypes[i]
                    + (colNames[i].equals(primaryCol) ? " PRIMARY KEY " + ", " : ", ");
        }// for i
        ddlStatement = ddlStatement.substring(0, ddlStatement.length() - 2) + ")";

        execute(ddlStatement);
    }

    public void deleteTable(String tableName) {
        try {
            execute("DROP TABLE IF EXISTS " + tableName);
        } catch (SQLException e) {
            System.err.println("Could not delete table - " + tableName + ". Ignoring..");
        }
    }

    public boolean tableExists(String tableName) throws SQLException {
        // if SHOW TABLES query returns one or more rows, the table exists
        Statement statement = connection.createStatement();
        try {
            statement.execute("SHOW TABLES FROM " + datasource + " LIKE '" + tableName + "'");
            return statement.getResultSet().getRow() > 0;
        } finally {
            statement.close();
        }
    }

    public void addIndex(String table, String indexName, String[] indexColumnNames) throws SQLException {
        // instantiate a new string buffer in which the query would be created
        StringBuffer sb = new StringBuffer("ALTER TABLE " + table + " ADD ");
        final String INDEX = "INDEX ";

        sb.append(INDEX + indexName + "(" + indexColumnNames[0]);
        for (int i = 1; i < indexColumnNames.length; i++) {
            sb.append(", " + indexColumnNames[i]);
        }
        sb.append(")");

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

        execute(sb.toString());
    }

    private String clean(String dirtyStr) {
        return dirtyStr.replace('-', '_');
    }

    private void execute(final String query) throws SQLException {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.execute(query);
        } finally {
            if (statement != null)
                statement.close();
        }
    }
}
