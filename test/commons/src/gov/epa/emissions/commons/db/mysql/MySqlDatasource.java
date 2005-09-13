package gov.epa.emissions.commons.db.mysql;

import gov.epa.emissions.commons.db.DataAcceptor;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.Query;
import gov.epa.emissions.commons.db.TableDefinition;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class MySqlDatasource implements Datasource, Cloneable, Serializable {

    private Connection connection;

    private DataAcceptor dataAcceptor;

    private String name;

    public MySqlDatasource(String name, Connection connection) {
        this.name = name;
        this.connection = connection;
        this.dataAcceptor = new DataAcceptor(connection);
    }

    public String getName() {
        return name;
    }

    public Connection getConnection() {
        return connection;
    }

    public void execute(final String query) throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute(query);
    }

    public DataAcceptor getDataAcceptor() {
        return dataAcceptor;
    }

    public Query query() {
        return new MySqlQuery(connection);
    }

    public TableDefinition tableDefinition() {
        return new MySqlTableDefinition(name, connection);
    }

}
