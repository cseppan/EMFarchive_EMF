package gov.epa.emissions.commons.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface Datasource {

    ConnectionParams getConnectionParams();

    String getName();

    ResultSet executeQuery(String query) throws SQLException;

    void execute(String query) throws SQLException;

    List getTableNames(boolean b);

    Connection getConnection();

    void insertRow(String tableName, String[] data, String[] colTypes) throws SQLException;

    void createTable(String tableName, String[] colNames, String[] colTypes, String[] primaryCols, boolean overwrite)
            throws SQLException;

    //TODO: combine datasource and dataacceptor
    AbstractDataAcceptor getDataAcceptor();
}
