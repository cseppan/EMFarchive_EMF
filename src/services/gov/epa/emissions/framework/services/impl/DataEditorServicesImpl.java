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
import gov.epa.emissions.framework.db.PageReader;
import gov.epa.emissions.framework.db.ScrollableRecords;
import gov.epa.emissions.framework.services.DataEditorServices;
import gov.epa.emissions.framework.services.EMFConstants;
import gov.epa.emissions.framework.services.Page;

import java.sql.SQLException;
import java.util.Date;
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
    
    private String name="Default";
    /**
     * 
     */
    public DataEditorServicesImpl() {
        super();
        readerMap = new HashMap();
        log.debug("CONSTRUCTOR HASHCODE: " + this.hashCode());
        try {
            Context ctx = new InitialContext();
            
            DataSource emfDatabase = (DataSource) ctx.lookup("java:/comp/env/jdbc/EMFDB");
            DbServer dbServer = new PostgresDbServer(emfDatabase.getConnection(), EMFConstants.EMF_REFERENCE_SCHEMA,
                    EMFConstants.EMF_EMISSIONS_SCHEMA);
            emissionsSchema = dbServer.getEmissionsDatasource();
            
        } catch (Exception ex) {
            log.error("could not initialize EMF datasource", ex);
            ex.printStackTrace();
//            throw new InfrastructureException("Server configuration error");
        }
    
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.services.DataEditorServices#getName()
     */
    public String getName() {
        log.debug("GET NAME HASHCODE: " + this.hashCode());
        return this.name;
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.services.DataEditorServices#setName(java.lang.String)
     */
    public void setName(String name) {
        log.debug("SETNAME HASHCODE: " + this.hashCode() +" name= " + name);
        this.name=name;
    }

    public Page getPage(String tableName, int pageNumber) {
        final int pageSize = 100;

        log.debug("Table name: " + tableName + " Page number: " + pageNumber);
        long startTime = new Date().getTime();
        PageReader reader = null;
        
        if (readerMap.containsKey(tableName)){
            log.debug("FOUND READER");
            reader = (PageReader)readerMap.get(tableName);
        }else{
            log.debug("NO READER FOUND");
            String query = "select * from " + emissionsSchema.getName()+"."+ tableName;
            log.debug("The query " + query);
            ScrollableRecords sr = new ScrollableRecords(emissionsSchema, query);
            reader = new PageReader(pageSize, sr);
            readerMap.put(tableName,reader);
        }
        try {
            reader.init();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        String msg = null;
        Page page = null;
        
        if (reader==null){
            msg = "reader is null";
        }else{
            
            try {
                log.debug("page number: " + pageNumber);
                page = reader.page(pageNumber);
                log.debug("page: " + page);
            } catch (Exception e) {
                log.debug("####################### " + e);
                e.printStackTrace();
            }
            if (page==null){
                msg = "Page is null";
            }else{
                msg = "Number of records in page= " + page.getRecords().length;
            }
        }
        log.debug(msg);
        long endTime = new Date().getTime();
        log.debug("START TIME= " + startTime);
        log.debug("End Time= " + endTime);
        log.debug("TIME LAG= " + (endTime-startTime));
        return page;
    }

    public int getPageCount(String tableName) throws EmfException {
        // TODO Auto-generated method stub
        
        if (false) throw new EmfException("");
        return 0;
    }

}
