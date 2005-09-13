package gov.epa.emissions.commons.db;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface Query {

    ResultSet executeQuery(String query) throws SQLException;

    void execute(String query) throws SQLException;

    ResultSet select(String[] columnNames, String table) throws SQLException;

    void insertRow(String table, String[] data, String[] colTypes) throws SQLException;

    ResultSet selectAll(String table) throws SQLException;

}
