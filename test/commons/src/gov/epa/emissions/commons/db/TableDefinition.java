package gov.epa.emissions.commons.db;

import java.sql.SQLException;
import java.util.List;

public interface TableDefinition {

    List getTableNames() throws SQLException;

    void createTableWithOverwrite(String tableName, String[] colNames, String[] colTypes, String[] primaryCols)
            throws SQLException;

    void createTable(String table, String[] colNames, String[] colTypes, String primaryCol) throws SQLException;

    void deleteTable(String tableName) throws SQLException;

    boolean tableExists(String tableName) throws Exception;

    /**
     * ALTER TABLE ADD INDEX indexName (indexColumnNames0, indexColumnNames1,
     * ....)
     */
    void addIndex(String table, String indexName, String[] indexColumnNames) throws SQLException;

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
     */
    void addColumn(String table, String columnName, String columnType, String afterColumnName) throws Exception;

}
