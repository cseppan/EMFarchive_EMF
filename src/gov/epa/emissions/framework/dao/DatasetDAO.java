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

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;



/**
 * @author Conrad F. D'Cruz
 *
 */
public class DatasetDAO {

//  private static final String GET_DATASET_QUERY="select aDset from Dataset as aDset";
private static final String GET_DATASET_QUERY="select aDset from EmfDataset as aDset";
  
  public static List getDatasets(Session session) throws EmfException{
      System.out.println("In getMessages");
      ArrayList datasets = new ArrayList();
      
      Transaction tx = session.beginTransaction();
      
      Query query = session.createQuery(GET_DATASET_QUERY);

      Iterator iter = query.iterate();
      while (iter.hasNext()){
          Dataset aDset = (Dataset)iter.next();
          datasets.add(aDset);  
      }
      
      tx.commit();
      System.out.println("End getMessages");
      return datasets;
  }//getDatasetTypes()
  
  public static void insertDatasetType(Dataset aDset, Session session){
      Transaction tx = session.beginTransaction();
      session.save(aDset);
      session.flush();
      tx.commit();
  }
  

}
