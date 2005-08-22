/*
 * Created on Aug 10, 2005
 *
 * Eclipse Project Name: EMF
 * Package: package gov.epa.emissions.framework.dao;
 * File Name: DataSourceFactory.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.dao;

import gov.epa.emissions.framework.InfrastructureException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Conrad F. D'Cruz
 * 
 */
public class DataSourceFactory {
    private static Log log = LogFactory.getLog(DataSourceFactory.class);

    public static DataSource getDataSource() throws InfrastructureException {
        log.debug("get data source");
        DataSource ds = null;
        try {
            Context ctx = new InitialContext();
            if (ctx == null)
                throw new Exception("No Context");
            log.debug("BEFORE: Is datasource null? " + (ds == null));
            ds = (DataSource) ctx.lookup("java:/comp/env/jdbc/EMFDB");
            log.debug("AFTER: Is datasource null? " + (ds == null));
        } catch (NamingException ex) {
            log.error(ex);
            throw new InfrastructureException("Server configuration error");
        } catch (Exception ex) {
            log.error(ex);
            throw new InfrastructureException("Server configuration error");
        }
        log.debug("get data source");
        return ds;
    }

}
