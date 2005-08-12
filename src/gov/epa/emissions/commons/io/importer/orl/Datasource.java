package gov.epa.emissions.commons.io.importer.orl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Craig Mattocks
 * @version $Id: Datasource.java,v 1.1 2005/08/12 14:12:14 rhavaldar Exp $
 */
public interface Datasource {

    // Types of databases
    public static final int REFERENCE = 0;

    public static final int ANALYSIS = 1;

    public static final int EMISSIONS = 2;

    ConnectionParams getConnectionParams();

    String getHost();

    String getName();

    String getPort();

    String getPassword();

    String getUser();

    Class[] getColumnClasses(String tableName) throws Exception;

    ResultSet executeQuery(String query) throws SQLException;

    String[] getColumnNames(String tableName) throws SQLException;

    String getType();

    void disconnect();

    void execute(String query) throws SQLException;

    List getTableNames(boolean b);

    Connection getConnection();

    void insertRow(String tableName, String[] data, String[] colTypes) throws SQLException;

    List getUniqueColumnValues(String tableName, String string, boolean ascending);

    void setServer(String server);

    void setHost(String host);

    void setPort(String port);

    void setName(String name);

    void setUser(String user);

    void setPassword(String password);

    void setConnection(Connection connection);

    void createTable(String tableName, String[] colNames, String[] colTypes, String primaryCol, boolean auto,
            boolean overwrite) throws Exception;

    void createTable(String tableName, String[] colNames, String[] colTypes, String[] primaryCols, boolean overwrite)
            throws SQLException;

    DataAcceptor getDataAcceptor();

    String getDriver();
}
