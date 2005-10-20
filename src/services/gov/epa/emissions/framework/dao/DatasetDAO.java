/*
 * Creation on Aug 29, 2005
 * Eclipse Project Name: EMF
 * File Name: DatasetDAO.java
 * Author: Conrad F. D'Cruz
 */

package gov.epa.emissions.framework.dao;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.Country;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.Sector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * @author Conrad F. D'Cruz
 * 
 */
public class DatasetDAO {
    private static Log log = LogFactory.getLog(DatasetDAO.class);

    private static final String GET_DATASET_QUERY = "select aDset from EmfDataset as aDset";
    private static final String GET_COUNTRY_QUERY = "select country from Country as country";
    private static final String GET_SECTOR_QUERY = "select sector from Sector as sector";

    private static final String GET_DATASET_FOR_DATASETNAME_QUERY = "select aDset from EmfDataset as aDset where aDset.name=:datasetname";

    /**
     * This method checks if the dataset name exists in the Datasets table A
     * dataset name is unique in the EMF system. If the name is already used by
     * another dataset record then return true else return false.
     * 
     * @param datasetName
     * @param session
     * @return
     * @throws EmfException
     */
    public static boolean isDatasetNameUsed(String datasetName, Session session) {
        boolean dsNameExists = false;
		Transaction tx = null;

        try {
			tx = session.beginTransaction();

			Query query = session.createQuery(GET_DATASET_FOR_DATASETNAME_QUERY);
			query.setParameter("datasetname", datasetName, Hibernate.STRING);

			Iterator iter = query.iterate();
			while (iter.hasNext()) {
			    dsNameExists = true;
			    break;
			}

			tx.commit();
		} catch (HibernateException e) {
			log.error(e);
			tx.rollback();
		}

        return dsNameExists;
    }// getDataset

    public static List getDatasets(Session session) {
        log.debug("In get all Datasets with valid session?: " + (session == null));
        ArrayList datasets = null;

		Transaction tx = null;

        try {
			datasets = new ArrayList();

			tx = session.beginTransaction();
			log.debug("The query: " + GET_DATASET_QUERY);
			Query query = session.createQuery(GET_DATASET_QUERY);

			Iterator iter = query.iterate();
			while (iter.hasNext()) {
			    EmfDataset aDset = (EmfDataset) iter.next();
			    datasets.add(aDset);
			}
	        log.info("Total number of datasets retrieved= " + datasets.size());

			tx.commit();
		} catch (HibernateException e) {
			log.error(e);
			tx.rollback();
		}
        log.debug("End getDatasets");
        return datasets;
    }// getDatasets()

    public static void insertDataset(EmfDataset dataset, Session session) {
        Transaction tx = null;
        
        try {
			tx = session.beginTransaction();
			session.save(dataset);
			tx.commit();
		} catch (HibernateException e) {
			log.error(e);
			tx.rollback();
		}
    }

    public static void updateDataset(EmfDataset dataset, Session session){
    	log.debug("updating dataset: " + dataset.getDatasetid());
        Transaction tx = null;
        
        try {
			tx = session.beginTransaction();
			session.update(dataset);
			tx.commit();    	
			log.debug("updating dataset: " + dataset.getDatasetid());
		} catch (HibernateException e) {
			log.error(e);
			tx.rollback();
		}
    }

	public static List getCountries(Session session) {
        log.debug("In get all Countries with valid session?: " + (session == null));
        ArrayList countries = null;

        Transaction tx=null;
        
        try {
			tx = session.beginTransaction();        
			countries= new ArrayList();
			log.debug("The query: " + GET_COUNTRY_QUERY);
			Query query = session.createQuery(GET_COUNTRY_QUERY);

			Iterator iter = query.iterate();
			while (iter.hasNext()) {
			    Country cntry = (Country) iter.next();
			    countries.add(cntry);
			}

			tx.commit();
			log.info("Total number of countries retrieved= " + countries.size());
		} catch (HibernateException e) {
			log.error(e);
			tx.rollback();
		}
        
		log.debug("End getSectors");
        
        return countries;
	}

	public static List getSectors(Session session) {
        log.debug("In get all Sectors with valid session?: " + (session == null));
        ArrayList sectors = new ArrayList();
        Transaction tx = null;

        try {
			tx = session.beginTransaction();
			sectors = new ArrayList();

			log.debug("The query: " + GET_SECTOR_QUERY);
			Query query = session.createQuery(GET_SECTOR_QUERY);

			Iterator iter = query.iterate();
			while (iter.hasNext()) {
				Sector sector = (Sector) iter.next();
				sectors.add(sector);
			}

			tx.commit();
			log.info("Total number of sectors retrieved= " + sectors.size());
		} catch (HibernateException e) {
			log.error(e);
			tx.rollback();
		}
        
        log.debug("End getSectors");
        return sectors;
	}

}
