package gov.epa.emissions.commons.db;

import java.sql.Connection;

public interface Datasource {

    Query query();

    TableDefinition tableDefinition();

    String getName();

    Connection getConnection();

    DataAcceptor getDataAcceptor();
}
