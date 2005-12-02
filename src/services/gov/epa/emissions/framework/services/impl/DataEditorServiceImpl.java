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
import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.PageReader;
import gov.epa.emissions.commons.db.postgres.PostgresDbServer;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.ScrollableVersionedRecords;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.InfrastructureException;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.EMFConstants;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
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
public class DataEditorServiceImpl implements DataEditorService {
    private static Log log = LogFactory.getLog(DataEditorServiceImpl.class);

    private Datasource emissionsSchema = null;

    private Map pageReadersMap = null;

    public DataEditorServiceImpl() throws InfrastructureException {
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
            return reader.totalPages();
        } catch (SQLException e) {
            log.error("Failed to get page count: " + e.getMessage());
            throw new EmfException(e.getMessage());
        }
    }

    private PageReader getReader(String tableName) throws SQLException {
        if (!pageReadersMap.containsKey(tableName)) {
            String query = "SELECT * FROM " + emissionsSchema.getName() + "." + tableName;
            ScrollableVersionedRecords sr = new ScrollableVersionedRecords(emissionsSchema, query);
            PageReader reader = new PageReader(20, sr);

            pageReadersMap.put(tableName, reader);
        }

        return (PageReader) pageReadersMap.get(tableName);
    }

    public Page getPageWithRecord(String tableName, int recordId) throws EmfException {
        try {
            PageReader reader = getReader(tableName);
            return reader.pageByRecord(recordId);
        } catch (SQLException ex) {
            log.error("Initialize reader: " + ex.getMessage());
            throw new EmfException("Page Reader error: " + ex.getMessage());
        }
    }

    public int getTotalRecords(String tableName) throws EmfException {
        try {
            PageReader reader = getReader(tableName);
            return reader.totalRecords();
        } catch (SQLException e) {
            log.error("Failed to get total records count: " + e.getMessage());
            throw new EmfException(e.getMessage());
        }
    }

    public void close() throws EmfException {
        Collection all = pageReadersMap.values();
        Iterator iter = all.iterator();
        while (iter.hasNext()) {
            try {
                PageReader pageReader = (PageReader) iter.next();
                pageReader.close();
            } catch (SQLException e) {
                log.error("Could not close 'query session' due to " + e.getMessage());
                throw new EmfException(e.getMessage());
            }
        }
        pageReadersMap.clear();
    }

    /**
     * This method is for cleaning up session specific objects within this
     * service.
     * 
     */
    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }

    public Version derive(Version baseVersion){
        // TODO Auto-generated method stub
        return null;
    }

    public void submit(ChangeSet changeset) {
        // TODO Auto-generated method stub
        
    }

    public void markFinal() {
        // TODO Auto-generated method stub
        
    }
}
