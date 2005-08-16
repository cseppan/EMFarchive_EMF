package gov.epa.emissions.commons.db;

import java.sql.Connection;

public class PostgresDataAcceptor extends AbstractDataAcceptor {

    public PostgresDataAcceptor(Connection connection) {
        super(connection);
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

        execute(ddlStatement);
    }

}
