package gov.epa.emissions.commons.db;

import java.sql.Connection;

public class PostgresDataAcceptor extends AbstractDataAcceptor {

    public PostgresDataAcceptor(Connection connection) {
        super(connection);
    }

    public String customizeCreateTableQuery(String query) {
        return query;
    }

    public void setTable(String tableName) {
        table = tableName;
    }

    public void createTable(String[] colNames, String[] colTypes, String primaryCol, boolean auto) throws Exception {
        String ddlStatement = "CREATE TABLE " + table + " (";

        for (int i = 0; i < colNames.length; i++) {
            // one of the columnnames was "dec" for december.. caused a problem
            // there
            if (colNames[i].equals("dec"))
                colNames[i] = colNames[i] + "1";

            ddlStatement = ddlStatement + clean(colNames[i]) + " " + colTypes[i]
                    + (colNames[i].equals(primaryCol) ? " PRIMARY KEY " + ", " : ", ");
        }
        ddlStatement = ddlStatement.substring(0, ddlStatement.length() - 2) + ")";

        ddlStatement = customizeCreateTableQuery(ddlStatement);

        execute(ddlStatement);
    }

    public void insertRow(String table, String[] data, String[] colTypes) throws Exception {
        StringBuffer sb = new StringBuffer("INSERT INTO " + table + " VALUES(");

        for (int i = 0; i < data.length; i++) {
            if (colTypes[i].startsWith("VARCHAR")) {
                String cleanedCell = clean(data[i]);

                // TODO: escape single quote
                String cellStrippedOffSingleQuotes = cleanedCell.replace('\'', ' ');
                sb.append("'" + cellStrippedOffSingleQuotes + "'");

            } else {
                if (data[i].trim().length() == 0)
                    data[i] = "NULL";
                sb.append(data[i]);
            }
            sb.append(',');
        }// for int i

        // there will an extra comma at the end so delete that
        sb.deleteCharAt(sb.length() - 1);
        sb.append(')');

        execute(sb.toString());
    }

    public void addIndex(String indexName, String[] indexColumnNames) throws Exception {
        StringBuffer sb = new StringBuffer();
        // postgres indexes must be unique across tables/database
        String syntheticIndexName = table.replace('.', '_') + "_" + indexName;
        sb.append("CREATE INDEX " + syntheticIndexName + " ON " + table + " (" + indexColumnNames[0]);
        for (int i = 1; i < indexColumnNames.length; i++) {
            sb.append(", " + indexColumnNames[i]);
        }
        sb.append(")");
        execute(sb.toString());
    }

    public void addColumn(String table, String columnName, String columnType, String afterColumnName) throws Exception {
        String statement = "ALTER TABLE " + table + " ADD " + columnName + " " + columnType;
        execute(statement);
    }

}
