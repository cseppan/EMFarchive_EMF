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
    private HashMap readerMap = null;

    private int pageSize = EMFConstants.PAGE_SIZE;
    
    /**
     * @throws InfrastructureException 
     * 
     */
    public DataEditorServicesImpl() throws InfrastructureException {
        super();
        readerMap = new HashMap();
        try {
            Context ctx = new InitialContext();
            
            DataSource emfDatabase = (DataSource) ctx.lookup("java:/comp/env/jdbc/EMFDB");
            DbServer dbServer = new PostgresDbServer(emfDatabase.getConnection(), EMFConstants.EMF_REFERENCE_SCHEMA,
                    EMFConstants.EMF_EMISSIONS_SCHEMA);
            emissionsSchema = dbServer.getEmissionsDatasource();
            
        } catch (Exception ex) {
            log.error("could not initialize EMF datasource", ex);
            ex.printStackTrace();
            throw new InfrastructureException("Server configuration error");
        }
    
    }

    public Page getPage(String tableName, int pageNumber) throws EmfException {
        PageReader reader = reader = getReader(tableName);
        String msg = null;
        Page page = null;
        
        try {
            reader.init();
            page = reader.page(pageNumber);
        } catch (SQLException ex) {
            log.error("Initialize reader: " + ex.getMessage());
            throw new EmfException("Page Reader error: " + ex.getMessage());
        }
        return page;
    }

    private PageReader getReader(String tableName) {
        PageReader reader = null;
        
        if (readerMap.containsKey(tableName)){
            reader = (PageReader)readerMap.get(tableName);
        }else{
            String query = "select * from " + emissionsSchema.getName()+"."+ tableName;
            ScrollableRecords sr = new ScrollableRecords(emissionsSchema, query);
            reader = new PageReader(pageSize, sr);
            readerMap.put(tableName,reader);
        }
        return reader;
    }

    public int getPageCount(String tableName) throws EmfException {
        int pageCount = -1;
        
        try {
            PageReader reader = reader = getReader(tableName);
            reader.init();
            pageCount = reader.count();
        } catch (SQLException e) {
            log.error("Failed to get page count: " + e.getMessage());
            throw new EmfException(e.getMessage());
        }
        
        return pageCount;      

    }

}
