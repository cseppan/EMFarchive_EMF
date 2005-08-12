package gov.epa.emissions.commons.io.importer.orl;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MySqlDatasource implements Datasource, Cloneable, Serializable {

    private Connection connection;

    private boolean recheck;

    private java.util.List tableNames = null;

    protected java.util.List columnNames = null;

    private volatile boolean connectedToDatabase = false;

    private DataAcceptor dataAcceptor;

    private ConnectionParams params;

    public MySqlDatasource(ConnectionParams params, Connection connection) {
        this.params = params;
        this.connection = connection;
        this.dataAcceptor = new MySqlDataAcceptor(params.getDatasource(), connection, false, false);
    }

    public List getColumnNames() {
        return columnNames;
    }

    public String[] getColumnNames(String tableName) throws SQLException {
        if (connection == null) {
            throw new SQLException("Please establish the connection with \"" + getName() + "\"");
        }
        Statement statement = connection.createStatement();
        statement.setMaxRows(1);
        ResultSet rs = statement.executeQuery("SELECT * FROM " + tableName);
        ResultSetMetaData metaData = rs.getMetaData();
        int colCount = metaData.getColumnCount();
        String[] columns = new String[colCount];
        for (int i = 0; i < colCount; i++) {
            columns[i] = metaData.getColumnName(i + 1); // add 1 for to start
            // the index from 1
        }
        rs.close();
        statement.close();
        return columns;
    }

    public Class[] getColumnClasses(String tableName) throws Exception {
        if (connection == null) {
            throw new SQLException("Please establish the connection with \"" + getName() + "\"");
        }
        Statement statement = connection.createStatement();
        statement.setMaxRows(1);
        ResultSet rs = statement.executeQuery("SELECT * FROM " + tableName);
        ResultSetMetaData metaData = rs.getMetaData();
        int count = metaData.getColumnCount();
        Class[] colClasses = new Class[count];
        for (int i = 0; i < count; i++) {
            colClasses[i] = Class.forName(metaData.getColumnClassName(i + 1));
        }
        rs.close();
        statement.close();
        return colClasses;
    }

    /**
     * Read and print the database's meta data
     */
    private void getDBMetaData() {
        try {

            // Get the meta data
            DatabaseMetaData metaData = connection.getMetaData();

            // Provide storage for table and column names
            tableNames = new ArrayList();
            columnNames = new ArrayList();

            // Scan the database's tables
            String[] validTypes = { "TABLE" };
            ResultSet theTables = metaData.getTables(null, null, null, validTypes);
            while (theTables.next()) {
                String tableName = theTables.getString("TABLE_NAME");
                tableNames.add(tableName);

                // Now get the columns in that table
                ResultSet theColumns = metaData.getColumns(null, null, tableName, null);
                while (theColumns.next()) {
                    String columnName = theColumns.getString("COLUMN_NAME");
                    columnNames.add(columnName);
                }
                // System.out.println();
            }
        } catch (Exception e) {
            System.out.println("Problems communicating with the database!");
        }
    } // end getDBMetaData method

    /**
     * Loads the appropriate driver class into the Java namespace
     * 
     * @throws Exception
     *             if the class could not be located
     */
    public void loadDriver(final String driver) throws Exception {
        // Load the DB driver.
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException cnfx) {
            throw new Exception("Can't load JDBC driver!");
        }
    }

    public void disconnect() {
    }

    public void setServer(final String server) {
    }

    public void setHost(final String host) {
    }

    public void setPort(final String port) {
    }

    public void setName(final String name) {
    }

    public void setDriver(final String driver) {
    }

    public void setUser(final String user) {
    }

    public void setPassword(final String password) {
    }

    public void setConnection(final Connection connection) {
        this.connection = connection;
    }

    public void setRecheck(final boolean recheck) {
        this.recheck = recheck;
    }

    public void setTableNames(final java.util.List tableNames) {
        this.tableNames = tableNames;
    }

    public void setColumnNames(final java.util.List columnNames) {
        this.columnNames = columnNames;
    }

    public void setConnectedToDatabase(final boolean connectedToDatabase) {
        this.connectedToDatabase = connectedToDatabase;
    }

    /**
     * Get (accessor) methods
     */
    public String getType() {
        return "mysql";
    }

    public String getHost() {
        return null;
    }

    public String getPort() {
        return null;
    }

    public String getName() {
        return params.getDatasource();
    }

    public String getDriver() {
        return "com.mysql.jdbc.Driver";
    }

    public String getUser() {
        return null;
    }

    public String getPassword() {
        return null;
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean getRecheck() {
        return recheck;
    }

    public List getTableNames(boolean recheck) {
        if (tableNames == null || recheck)
            getDBMetaData();
        return tableNames;
    }

    public java.util.List getTableNames() {
        return tableNames;
    }

    public boolean isConnected() {
        return connectedToDatabase;
    }

    public boolean getConnectedToDatabase() {
        return connectedToDatabase;
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
    public void createTable(String tableName, String[] colNames, String[] colTypes, String[] primaryCols,
            boolean overwrite) throws SQLException {
        // check to see if there are the same number of column names and column
        // types
        int length = colNames.length;
        if (length != colTypes.length)
            throw new SQLException("There are different numbers of column names and types");

        if (overwrite) {
            try {
                // TODO: make db pluggable
                execute("DROP TABLE " + tableName);
            } catch (Exception e) {
                // TODO: ignore (for postgress)
            }
        }

        String queryString = "CREATE TABLE " + tableName + " (";

        for (int i = 0; i < length - 1; i++) {
            queryString += MySqlDatasource.clean(colNames[i]) + " " + colTypes[i] + ", ";
        }// for i
        queryString += MySqlDatasource.clean(colNames[length - 1]) + " " + colTypes[length - 1];

        String primaryColumns = "";
        if (primaryCols != null && primaryCols.length != 0) {
            primaryColumns = ", PRIMARY KEY (";
            for (int i = 0; i < primaryCols.length - 1; i++) {
                primaryColumns += MySqlDatasource.clean(primaryCols[i]) + ", ";
            }
            primaryColumns += MySqlDatasource.clean(primaryCols[primaryCols.length - 1]) + " )";
        }

        queryString = queryString + primaryColumns + ")";
        execute(queryString);
    }

    /**
     * Create the table using the header RP: WARNING: auto can be specified for
     * integer colTypes only. we have to change this method
     */
    public void createTable(String tableName, String[] colNames, String[] colTypes, String primaryCol, boolean auto,
            boolean overwrite) throws Exception {
        // check to see if there are the same number of column names and column
        // types
        if (colNames.length != colTypes.length)
            throw new Exception("There are different numbers of column names and types");

        if (overwrite) {
            execute("DROP TABLE IF EXISTS " + tableName);
        }

        String queryString = "CREATE TABLE IF NOT EXISTS " + getName() + "." + tableName + " (";

        for (int i = 0; i < colNames.length; i++) {
            // one of the columnnames was "dec" for december.. caused a problem
            // there
            if (colNames[i].equals("dec"))
                colNames[i] = colNames[i] + "1";

            queryString = queryString + MySqlDatasource.clean(colNames[i]) + " " + colTypes[i]
                    + (colNames[i].equals(primaryCol) ? " PRIMARY KEY " + (auto ? " AUTO_INCREMENT, " : ", ") : ", ");
        }// for i

        queryString = queryString.substring(0, queryString.length() - 2) + ")";
        execute(queryString);
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
                String cleanedCell = MySqlDatasource.clean(data[i]);
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

    /**
     * Obtain unique values in a column from a table in an SQL database.
     */
    public List getUniqueColumnValues(final String tableName, final String columnName, boolean ascending) {
        List uniqueValues = null;

        try {
            String orderStyle = ascending ? "ASC" : "DESC";
            // @TODO: Use a PreparedStatement for this
            final ResultSet rs = executeQuery("SELECT DISTINCT " + columnName + " FROM " + tableName + " ORDER BY "
                    + columnName + " " + orderStyle);

            uniqueValues = new ArrayList();
            while (rs.next())
                uniqueValues.add(rs.getObject(1));
        } catch (Exception x) {
        }

        return uniqueValues;
    }

    /**
     * gets rid of unwanted characters
     */
    public static String clean(String dirtyStr) {
        return dirtyStr.replace('-', '_');
    }

    public DataAcceptor getDataAcceptor() {
        return dataAcceptor;
    }

    public ConnectionParams getConnectionParams() {
        return params;
    }

}
