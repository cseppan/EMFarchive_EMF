package gov.epa.emissions.commons.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * An acceptor which takes in data and puts it in a database
 * 
 * @author Prashant Pai, CEP UNC
 * @version $Id: DataAcceptor.java,v 1.1 2005/08/12 15:46:42 rhavaldar Exp $
 * @see MySqlDataAcceptor.java
 */

public abstract class DataAcceptor {

    /**
     * The database connection. need to store the connection since we would want
     * to close it at the end
     */
    protected Connection connection = null;

    /** a prepared statement to use * */
    protected PreparedStatement prepStatement = null;

    /** The name of the table that we will be updating. */
    protected String table = null;

    /** The prefix for the insert statements. */
    protected String insertPrefix = null;

    /** The prefix for the alter statements. */
    protected String alterAddPrefix = null;

    protected String alterDropPrefix = null;

    protected String alterModifyPrefix = null;

    /** The prefix for the update statements. */
    protected String updatePrefix = null;

    /** the column names * */
    protected String[] colNames;

    /** the column types * */
    protected String[] colTypes;

    /** if transactions need to be used * */
    protected boolean useTransactions = false;

    public DataAcceptor(Connection connection, boolean useTransactions, boolean usePrepStatement) {
        this.connection = connection;
        this.useTransactions = useTransactions;
    }

    /**
     * Open the database connection and get a Statement ready.
     * 
     * @throws Exception
     *             when either the database or table has not been set
     */
    public void startAcceptingData() throws Exception {
    } // startAcceptingData()

    /**
     * Set the name of the table to write data to
     */
    public void setTable(String tableName) {
        table = tableName;
        insertPrefix = "INSERT INTO " + table + " VALUES(";
        alterAddPrefix = "ALTER TABLE " + table + " ADD ";
        alterDropPrefix = "ALTER TABLE " + table + " DROP ";
        alterModifyPrefix = "ALTER TABLE " + table + " MODIFY ";
        updatePrefix = "UPDATE " + table + " SET ";
    } // setTable()

    /**
     * gets rid of unwanted characters
     * 
     * @param dirtyStr
     *            the string to be cleaned
     * @return String the cleaned up string
     */
    public String clean(String dirtyStr) {
        // currently only remove " and replace by blank
        return dirtyStr.replace('"', ' ');
    }

    /**
     * Create the table using the header
     */
    public void createTable(String[] colNames, String[] colTypes, String primaryCol, boolean auto) throws Exception {
        // check to see if there are the same number of column names and column
        // types
        if (colNames.length != colTypes.length)
            throw new Exception("There are different numbers of column names and types");

        this.colNames = colNames;
        this.colTypes = colTypes;

        String ddlStatement = "CREATE TABLE " + table + " (";

        for (int i = 0; i < colNames.length; i++) {
            // one of the columnnames was "dec" for december.. caused a problem
            // there
            if (colNames[i].equals("dec"))
                colNames[i] = colNames[i] + "1";

            ddlStatement = ddlStatement + clean(colNames[i]) + " " + colTypes[i]
                    + (colNames[i].equals(primaryCol) ? " PRIMARY KEY " + (auto ? " AUTO_INCREMENT, " : ", ") : ", ");
        }// for i
        ddlStatement = ddlStatement.substring(0, ddlStatement.length() - 2) + ")";

        ddlStatement = customizeCreateTableQuery(ddlStatement);

        execute(ddlStatement);
    }// createTable()

    public abstract String customizeCreateTableQuery(String origQueryString);

    public abstract boolean tableExists(String tableName) throws Exception;

    abstract public void deleteTable(String tableName) throws SQLException;

    public void prepareForInsert(int numberOfValues) throws Exception {
        String questionMarks = "";
        for (int i = 0; i < numberOfValues; i++) {
            questionMarks += ", ?";
        }// for int i
        questionMarks = questionMarks.substring(2);
        String finalPrepQuery = insertPrefix + questionMarks + ")";
        prepStatement = connection.prepareStatement(finalPrepQuery);
    }// prepareForInsert(int)

    public void insertRow(String[] data, String[] colTypes) throws Exception {
        insertStandardRow(data, colTypes);
    }

    abstract protected void insertStandardRow(String[] data, String[] colTypes) throws Exception;

    /**
     * Alter the table by adding a new column of the specified type in the
     * specified location.
     * 
     * ALTER TABLE databaseName.tableName ADD columnName columnType [AFTER
     * afterColumnName]
     * 
     * @param columnName -
     *            the name of the new column to add
     * @param columnType -
     *            the type of the new column
     * @param afterColumnName -
     *            the column name to add the new column after. Use null for
     *            default function (add to end)
     * @throws Exception
     *             if encounter error altering table
     */
    public void addColumn(String columnName, String columnType, String afterColumnName) throws Exception {
        // instantiate a new string buffer in which the query would be created
        StringBuffer sb = new StringBuffer(alterAddPrefix);
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
     * Alter the table by dropping the specified column name.
     * 
     * ALTER TABLE databaseName.tableName DROP columnName
     * 
     * @param columnName -
     *            the name of the column to drop
     * @throws Exception
     */
    public void dropColumn(String columnName) throws Exception {
        String dropStatement = alterDropPrefix + columnName;
        execute(dropStatement);
    }// dropColumn(String)

    protected void execute(String query) throws SQLException {
        Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        try {
            statement.execute(query);
        } finally {
            statement.close();
        }
    }

    /**
     * ALTER TABLE databaseName.tableName MODIFY columnName columnType AFTER
     * afterColumnName
     * 
     * @param columnName
     * @param columnType
     * @param afterColumnName
     * @throws Exception
     */
    public void modifyColumn(String columnName, String columnType, String afterColumnName) throws Exception {
        final String AFTER = " AFTER ";
        String modifyStatement = alterModifyPrefix + columnName + " " + columnType + AFTER + afterColumnName;
        execute(modifyStatement);
    }// modifyColumn(String, String, String)

    /**
     * Alter the table by adding an index composed of <i>i</i> column names.
     * 
     * ALTER TABLE ADD INDEX indexName (indexColumnNames[i])
     * 
     * @param indexName
     * @param indexColumnNames
     * @throws Exception
     */
    public void addIndex(String indexName, String[] indexColumnNames) throws Exception {
        // instantiate a new string buffer in which the query would be created
        StringBuffer sb = new StringBuffer(alterAddPrefix);
        final String INDEX = "INDEX ";

        sb.append(INDEX + indexName + "(" + indexColumnNames[0]);
        for (int i = 1; i < indexColumnNames.length; i++) {
            sb.append(", " + indexColumnNames[i]);
        }
        sb.append(")");

        execute(sb.toString());
    }// addIndex(String, String[])

    /**
     * UPDATE databaseName.tableName SET columnName = setExpr WHERE
     * whereColumns[i] LIKE 'likeClauses[i]'
     * 
     * @param columnName -
     *            the column to update
     * @param setExpr -
     *            the expression used to update the column value
     * @param whereColumns -
     *            left hand sides of LIKE expressions for WHERE
     * @param likeClauses -
     *            right hand sides of LIKE expressions for WHERE
     * @throws Exception
     *             if encounter error updating table
     */
    public void updateWhereLike(String columnName, String setExpr, String[] whereColumns, String[] likeClauses)
            throws Exception {
        if (whereColumns.length != likeClauses.length) {
            throw new Exception("There are different numbers of WHERE column names and LIKE clauses");
        }

        // instantiate a new string buffer in which the query would be created
        StringBuffer sb = new StringBuffer(updatePrefix + columnName + " = " + setExpr + " WHERE ");

        // add the first LIKE expression
        sb.append(whereColumns[0] + " LIKE '" + likeClauses[0] + "'");

        // if there is more than one LIKE expression, add
        // "AND" before each of the remaining expressions
        for (int i = 1; i < whereColumns.length; i++) {
            sb.append(" AND " + whereColumns[i] + " LIKE '" + likeClauses[i] + "'");
        }

        execute(sb.toString());
    }// updateWhereLike(String, String, String[], String[])

    /**
     * UPDATE databaseName.tableName SET columnName = setExpr WHERE
     * whereColumns[i] = equalsClauses[i]
     * 
     * @param columnName -
     *            the column to update
     * @param setExpr -
     *            the expression used to update the column value
     * @param whereColumns -
     *            left hand sides of = expressions for WHERE
     * @param equalsClauses -
     *            right hand sides of = expressions for WHERE
     * @throws Exception
     *             if encounter error updating table
     */
    public void updateWhereEquals(String columnName, String setExpr, String[] whereColumns, String[] equalsClauses)
            throws Exception {
        if (whereColumns.length != equalsClauses.length) {
            throw new Exception("There are different numbers of WHERE column names and = clauses");
        }

        // instantiate a new string buffer in which the query would be created
        StringBuffer sb = new StringBuffer(updatePrefix + columnName + " = " + setExpr + " WHERE ");

        // add the first LIKE expression
        sb.append(whereColumns[0] + " = " + equalsClauses[0]);

        // if there is more than one LIKE expression, add
        // "AND" before each of the remaining expressions
        for (int i = 1; i < whereColumns.length; i++) {
            sb.append(" AND " + whereColumns[i] + " = " + equalsClauses[i]);
        }

        execute(sb.toString());
    }// updateWhereEquals(String, String, String[], String[])

    /**
     * Generate a concat expression for usage in SQL statements. If the value to
     * be concatenated is a literal (constant), it should be enclosed in ''.
     * 
     * @param exprs -
     *            Array of data ('literals' and column names)
     * @return the SQL concat expression
     */
    public String generateConcatExpr(String[] exprs) {
        final String CONCAT = "concat(";
        // begin the concat expression
        StringBuffer concat = new StringBuffer(CONCAT);
        // add the first string
        concat.append(exprs[0]);
        // if there is more than one string to concat, add a
        // comma separator before each of the remaining strings
        for (int i = 1; i < exprs.length; i++) {
            concat.append("," + exprs[i]);
        }
        // close off the concat expression
        concat.append(")");

        return concat.toString();
    }// generateConcatExpr(String[])

    abstract public ResultSet select(String[] columnNames, String tableName) throws Exception;

    /**
     * SELECT * FROM dbName.tableName
     * 
     * @param tableName -
     *            the table containing the data
     * 
     * @throws Exception
     *             if encounter error selecting table
     */
    public ResultSet selectAll(String tableName) throws Exception {
        return select(new String[] { "*" }, tableName);
    }// selectAll(String, String)

    /**
     * Close the database connection.
     * 
     * @throws Exception
     *             on closing connection to database.
     */
    public void finishAcceptingData() throws Exception {
        if (useTransactions) {
            execute("COMMIT");
        }
    }

    public List showColumnsLike(String columnLike) throws Exception {
        DatabaseMetaData metadata = this.connection.getMetaData();
        ResultSet result = metadata.getColumns(connection.getCatalog(), null, table, columnLike);
        List columns = new ArrayList();
        while (result.next()) {
            columns.add(result.getString("COLUMN_NAME"));
        }

        return columns;
    }
}
