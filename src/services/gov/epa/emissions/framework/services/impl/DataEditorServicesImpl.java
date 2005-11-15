/*
 * Creation on Nov 7, 2005
 * Eclipse Project Name: EMF
 * File Name: DataEditorServicesImpl.java
 * Author: Conrad F. D'Cruz
 */
/**
 * 
 */

package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.postgres.PostgresDbServer;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.InfrastructureException;
import gov.epa.emissions.framework.db.PageReader;
import gov.epa.emissions.framework.db.ScrollableRecords;
import gov.epa.emissions.framework.services.DataEditorServices;
import gov.epa.emissions.framework.services.EMFConstants;
import gov.epa.emissions.framework.services.Page;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Conrad F. D'Cruz
 * 
 */
public class DataEditorServicesImpl implements DataEditorServices {
    private static Log log = LogFactory.getLog(DataEditorServicesImpl.class);

    private Datasource emissionsSchema = null;

    private Map pageReadersMap = null;

    public DataEditorServicesImpl() throws InfrastructureException {
        super();
        pageReadersMap = new HashMap();
        try {
            Context ctx = new InitialContext();

            DataSource emfDatabase = (DataSource) ctx.lookup("java:/comp/env/jdbc/EMFDB");
            DbServer dbServer = new PostgresDbServer(emfDatabase.getConnection(), EMFConstants.EMF_REFERENCE_SCHEMA,
                    EMFConstants.EMF_EMISSIONS_SCHEMA);
            emissionsSchema = dbServer.getEmissionsDatasource();

        } catch (Exception ex) {
            log.error("could not initialize EMF datasource", ex);
            throw new InfrastructureException("Server configuration error");
        }

    }

    public Page getPage(String tableName, int pageNumber) throws EmfException {
        try {
            PageReader reader = getReader(tableName);
            return reader.page(pageNumber);
        } catch (SQLException ex) {
            log.error("Initialize reader: " + ex.getMessage());
            throw new EmfException("Page Reader error: " + ex.getMessage());
        }
    }

    public int getPageCount(String tableName) throws EmfException {
        try {
            PageReader reader = getReader(tableName);
            return reader.pageCount();
        } catch (SQLException e) {
            log.error("Failed to get page count: " + e.getMessage());
            throw new EmfException(e.getMessage());
        }
    }

    private PageReader getReader(String tableName) throws SQLException {
        if (!pageReadersMap.containsKey(tableName)) {
            String query = "SELECT * FROM " + emissionsSchema.getName() + "." + tableName;
            ScrollableRecords sr = new ScrollableRecords(emissionsSchema, query);
            PageReader reader = new PageReader(20, sr);
            reader.init();

            pageReadersMap.put(tableName, reader);
        }

        return (PageReader) pageReadersMap.get(tableName);
    }
}
