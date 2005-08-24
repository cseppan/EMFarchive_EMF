/*
 * Created on Jul 29, 2005
 *
 * Eclipse Project Name: EMF
 * Package: package gov.epa.emissions.framework.dao;
 * File Name: DatasetTypesDAO.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.dao;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.EmfException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * @author Conrad F. D'Cruz
 *
 */
public class DatasetTypesDAO {
    private static Log log = LogFactory.getLog(DatasetTypesDAO.class);

    private static final String GET_DATASETTYPE_QUERY="select dst from DatasetType as dst";
    
    public static List getDatasetTypes(Session session) throws EmfException{
        log.debug("In getMessages");
        ArrayList datasetTypes = new ArrayList();
        
        Transaction tx = session.beginTransaction();
        
        Query query = session.createQuery(GET_DATASETTYPE_QUERY);

        Iterator iter = query.iterate();
        while (iter.hasNext()){
            DatasetType aDst = (DatasetType)iter.next();
            datasetTypes.add(aDst);  
        }
        
        tx.commit();
        log.debug("End getMessages");
        return datasetTypes;
    }//getDatasetTypes()
    
    public static void insertDatasetType(DatasetType aDst, Session session){
    	log.debug("inserting datatype into database");
        Transaction tx = session.beginTransaction();
        session.save(aDst);
        session.flush();
        tx.commit();
    	log.debug("inserting datatype into database");
    }
    

}
