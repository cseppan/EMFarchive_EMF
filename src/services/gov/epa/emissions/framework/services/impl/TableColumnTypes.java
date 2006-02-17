package gov.epa.emissions.framework.services.impl;

import java.sql.SQLException;

import javax.sql.DataSource;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.postgres.PostgresDbServer;
import gov.epa.emissions.framework.InfrastructureException;
import gov.epa.emissions.framework.dao.DataSourceFactory;
import gov.epa.emissions.framework.services.EMFConstants;

public class TableColumnTypes {
    protected DbServer dbServer;

    protected DataSource datasource;


    public TableColumnTypes() throws SQLException, InfrastructureException {
        super();
        datasource = new DataSourceFactory().get();

        dbServer = new PostgresDbServer(datasource.getConnection(), EMFConstants.EMF_REFERENCE_SCHEMA,
                EMFConstants.EMF_EMISSIONS_SCHEMA);
    }

    public static void main(String[] args) {
        try {
            new TableColumnTypes();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InfrastructureException e) {
            e.printStackTrace();
        }
    }

}
