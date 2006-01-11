package gov.epa.emissions.framework.dao;

import gov.epa.emissions.framework.InfrastructureException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

public class DataSourceFactory {

    public DataSource get() throws InfrastructureException {
        DataSource ds = null;
        try {
            Context ctx = new InitialContext();
            ds = (DataSource) ctx.lookup("java:/comp/env/jdbc/EMFDB");
        } catch (Exception ex) {
            throw new InfrastructureException("Unable to lookup Datasource using JNDI");
        }

        return ds;
    }

}
