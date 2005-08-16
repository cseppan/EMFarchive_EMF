package gov.epa.emissions.commons.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class AbstractDataAcceptor {

    protected Connection connection = null;

    protected String table = null;

    public AbstractDataAcceptor(Connection connection) {
        this.connection = connection;
    }

    public void setTable(String tableName) {
        table = tableName;
    }

    /**
     * gets rid of unwanted characters
     */
    protected String clean(String dirtyStr) {
        return dirtyStr.replace('"', ' ');
    }

    protected void execute(String query) throws SQLException {
        Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        try {
            statement.execute(query);
        } finally {
            statement.close();
        }
    }

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
        StringBuffer sb = new StringBuffer("UPDATE " + table + " SET " + columnName + " = " + setExpr + " WHERE ");

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
        StringBuffer sb = new StringBuffer("UPDATE " + table + " SET " + columnName + " = " + setExpr + " WHERE ");

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

}
