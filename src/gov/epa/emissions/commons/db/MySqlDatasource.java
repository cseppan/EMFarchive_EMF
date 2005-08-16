package gov.epa.emissions.commons.db;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MySqlDatasource implements Datasource, Cloneable, Serializable {

    private Connection connection;

    private DataAcceptor dataAcceptor;

    private String name;

    public MySqlDatasource(String name, Connection connection) {
        this.name = name;
        this.connection = connection;
        this.dataAcceptor = new DataAcceptor(connection);
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

    public ResultSet executeQuery(final String query) throws SQLException {
        Statement statement = connection.createStatement();
        return statement.executeQuery(query);
    }

    public void execute(final String query) throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute(query);
    }

    /**
     * Create the table using the header with multiple primary colums NOTE:
     * please ensure that primaryCols is a subset of colNames before calling
     * this method
     */
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

    public void createTable(String table, String[] colNames, String[] colTypes, String primaryCol)
            throws SQLException {
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
                String cleanedCell = clean(data[i]);
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

    private String clean(String dirtyStr) {
        return dirtyStr.replace('-', '_');
    }

    public DataAcceptor getDataAcceptor() {
        return dataAcceptor;
    }

    public ResultSet select(String[] columnNames, String tableName) throws SQLException {
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

    public void deleteTable(String tableName) throws SQLException {
        try {
            execute("DROP TABLE IF EXISTS " + tableName);
        } catch (SQLException e) {
            System.err.println("Could not delete table - " + tableName + ". Ignoring..");
        }
    }

    public boolean tableExists(String tableName) throws Exception {
        // if SHOW TABLES query returns one or more rows, the table exists
        Statement statement = connection.createStatement();
        try {
            statement.execute("SHOW TABLES FROM " + name + " LIKE '" + tableName + "'");
            return statement.getResultSet().getRow() > 0;
        } finally {
            statement.close();
        }
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
    
    /**
     * ALTER TABLE ADD INDEX indexName (indexColumnNames0, indexColumnNames1,
     * ....)
     */
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
    }// addIndex(String, String[])

}
