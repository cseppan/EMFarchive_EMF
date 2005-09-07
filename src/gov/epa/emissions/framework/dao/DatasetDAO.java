/*
 * Creation on Aug 29, 2005
 * Eclipse Project Name: EMF
 * File Name: DatasetDAO.java
 * Author: Conrad F. D'Cruz
 */

package gov.epa.emissions.framework.dao;

import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.framework.EmfException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;



/**
 * @author Conrad F. D'Cruz
 *
 */
public class DatasetDAO {
    private static Log log = LogFactory.getLog(StatusDAO.class);

	private static final String GET_DATASET_QUERY="select aDset from EmfDataset as aDset";
	private static final String GET_DATASET_FOR_DATASETNAME_QUERY="select aDset from EmfDataset as aDset where aDset.name=:datasetname";

	/**
	 * This method checks if the dataset name exists in the Datasets table
	 * A dataset name is unique in the EMF system.  If the name is already used 
	 * by another dataset record then return true else return false.
	 * 
	 * @param datasetName
	 * @param session
	 * @return
	 * @throws EmfException
	 */
	public static boolean isDatasetNameUsed(String datasetName, Session session) throws EmfException {
	  boolean dsNameExists= false;
	  
      Transaction tx = session.beginTransaction();
      
      Query query = session.createQuery(GET_DATASET_FOR_DATASETNAME_QUERY);
      query.setParameter("datasetname", datasetName, Hibernate.STRING);

      Iterator iter = query.iterate();
      while (iter.hasNext()){
    	  dsNameExists=true;
    	  break;
      }
      
      tx.commit();
	  
	  return dsNameExists;
  }//getDataset
  
  
  public static List getDatasets(Session session) throws EmfException{
      log.debug("In get all Datasets with valid session?: " + (session == null));
      ArrayList datasets = new ArrayList();
      
      Transaction tx = session.beginTransaction();
      log.debug("The query: " + GET_DATASET_QUERY);
      Query query = session.createQuery(GET_DATASET_QUERY);

      Iterator iter = query.iterate();
      while (iter.hasNext()){
          Dataset aDset = (Dataset)iter.next();
          datasets.add(aDset);  
      }
      
      tx.commit();
      log.info("Total number of datasets retrieved= " + datasets.size());
      log.debug("End getMessages");
      return datasets;
  }//getDatasetTypes()
  
  public static void insertDataset(Dataset aDset, Session session){
      Transaction tx = session.beginTransaction();
      session.save(aDset);
      tx.commit();
  }
  

}
