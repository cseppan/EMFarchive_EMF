package gov.epa.emissions.commons.io.importer.orl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PostgresDatasource implements Datasource {

    private Connection connection;

    private List tableNames = null;

    private volatile boolean connectedToDatabase = false;

    private PostgresDataAcceptor dataAcceptor;

    private ConnectionParams connectionParams;

    public PostgresDatasource(ConnectionParams params, Connection connection) {
        this.connection = connection;
        this.connectionParams = params;
        this.dataAcceptor = new PostgresDataAcceptor(connection, false, true);
    }

    public List getColumnNames() {
        return null;
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
            throw new Exception("Please establish the connection with \"" + getName() + "\"");
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
    public void getDBMetaData() {
        try {
            // Get the meta data
            DatabaseMetaData metaData = connection.getMetaData();

            // Provide storage for table and column names
            tableNames = new ArrayList();
            List columnNames = new ArrayList();

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
            }
        } catch (Exception e) {
            System.out.println("Problems communicating with the database!");
        }
    }

    /**
     * Disconnect a database.
     */
    public void disconnect() {// TODO: should disconnect ?
    }

    public void setId(final Long id) {
    }

    public void setType(final int type) {
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

    public void setTableNames(final java.util.List tableNames) {
        this.tableNames = tableNames;
    }

    /**
     * Get (accessor) methods
     */
    public String getType() {
        return "postgresql";
    }

    public String getHost() {
        return null;
    }

    public String getPort() {
        return null;
    }

    public String getName() {
        return connectionParams.getDatasource();
    }

    public String getDriver() {
        return "org.postgresql.Driver";
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

    public List getTableNames(boolean recheck) {
        if (tableNames == null || recheck)
            getDBMetaData();
        return tableNames;
    }

    public List getTableNames() {
        return tableNames;
    }

    public boolean isConnected() {
        return connectedToDatabase;
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
        // check to see if there are the same number of column names and column
        // types
        int length = colNames.length;
        if (length != colTypes.length)
            throw new SQLException("There are different numbers of column names and types");

        if (overwrite) {
            try {
                // TODO: make db pluggable
                execute("DROP TABLE " + connectionParams.getDatasource() + "." + tableName);
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
                primaryColumns += MySqlDatasource.clean(primaryCols[i]) + ", ";
            }
            primaryColumns += clean(primaryCols[primaryCols.length - 1]) + " )";
        }
        queryString = queryString + primaryColumns + ")";

        execute(queryString);
    }

    private String clean(String data) {
        return (data).replace('-', '_');
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
            execute("DROP TABLE " + getName() + "." + tableName);
        }

        String queryString = "CREATE TABLE " + getName() + "." + tableName + " (";

        for (int i = 0; i < colNames.length; i++) {
            // one of the columnnames was "dec" for december.. caused a problem
            // there
            if (colNames[i].equals("dec"))
                colNames[i] = colNames[i] + "1";

            String cleanedColumn = colNames[i].replace('-', '_');
            queryString = queryString + cleanedColumn + " " + colTypes[i]
                    + (colNames[i].equals(primaryCol) ? " PRIMARY KEY " + (auto ? " AUTO_INCREMENT, " : ", ") : ", ");
        }// for i
        queryString = queryString.substring(0, queryString.length() - 2) + ")";

        execute(queryString);
    }// createTable()

    /**
     * @param data
     *            to be put into the table
     * @param colTypes
     *            the data types
     * @throws Exception
     *             if error entering data
     */
    public void insertRow(String table, String[] data, String[] colTypes) throws SQLException {
        String insertPrefix = "INSERT INTO " + getName() + "." + table + " VALUES(";

        // instantiate a new string buffer in which the query would be created
        StringBuffer sb = new StringBuffer(insertPrefix);

        // append data to the query.. put quotes around VARCHAR entries
        for (int i = 0; i < data.length; i++) {
            if (colTypes[i].startsWith("VARCHAR")) {
                String cleanedCell = data[i].replace('-', '_');
                String cellWithSinglQuotesEscaped = cleanedCell.replace('\'', ' ');
                sb.append("'" + cellWithSinglQuotesEscaped + "'");
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
    } // acceptDataRow()

    /**
     * Obtain unique values in a column from a table in an SQL database.
     */
    public List getUniqueColumnValues(final String tableName, final String columnName, boolean ascending) {
        java.util.List uniqueValues = null;

        try {
            String orderStyle = ascending ? "ASC" : "DESC";
            // TODO: Use a PreparedStatement for this
            final ResultSet rs = executeQuery("SELECT DISTINCT " + columnName + " FROM " + getName() + "." + tableName
                    + " ORDER BY " + columnName + " " + orderStyle);

            uniqueValues = new ArrayList();
            while (rs.next())
                uniqueValues.add(rs.getObject(1));
        } catch (SQLException sqlx) {
        } catch (Exception x) {
        }

        return uniqueValues;
    }

    /**
     * Creates a new object of the same class and with the same contents as this
     * object.
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException cnsx) {
            throw new InternalError();
        }
    }

    public DataAcceptor getDataAcceptor() {
        return dataAcceptor;
    }

    public ConnectionParams getConnectionParams() {
        return connectionParams;
    }

}
