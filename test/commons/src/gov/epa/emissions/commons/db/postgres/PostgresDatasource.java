package gov.epa.emissions.commons.db.postgres;

import gov.epa.emissions.commons.db.DataAcceptor;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.Query;
import gov.epa.emissions.commons.db.TableDefinition;

import java.sql.Connection;

public class PostgresDatasource implements Datasource {

    private Connection connection;

    private DataAcceptor dataAcceptor;

    private String name;

    public PostgresDatasource(String name, Connection connection) {
        this.connection = connection;
        this.name = name;
        this.dataAcceptor = new DataAcceptor(connection);
    }

    public String getName() {
        return name;
    }

    public Connection getConnection() {
        return connection;
    }

    public DataAcceptor getDataAcceptor() {
        return dataAcceptor;
    }

    public Query query() {
        return new PostgresQuery(connection);
    }

    public TableDefinition tableDefinition() {
        return new PostgresTableDefinition(connection);
    }

}
