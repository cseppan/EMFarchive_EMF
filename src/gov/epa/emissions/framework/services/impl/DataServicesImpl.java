/*
 * Creation on Aug 29, 2005
 * Eclipse Project Name: EMF
 * File Name: DataServicesImpl.java
 * Author: Conrad F. D'Cruz
 */
/**
 * 
 */

package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.dao.DatasetDAO;
import gov.epa.emissions.framework.services.DataServices;
import gov.epa.emissions.framework.services.User;

import java.util.List;

import org.hibernate.Session;


/**
 * @author Conrad F. D'Cruz
 *
 */
public class DataServicesImpl implements DataServices {

	/**
	 * 
	 */
	public DataServicesImpl() {
		super();
	}

	public Dataset[] getDatasets() throws EmfException {
        Session session = HibernateUtils.currentSession();
        List datasets = DatasetDAO.getDatasets(session);
        System.out.println("Total number of datasets in the List= " + datasets.size());
        return (Dataset[]) datasets.toArray(new EmfDataset[datasets.size()]);    
	}

	public Dataset[] getDatasets(User user) throws EmfException {
		// TODO Auto-generated method stub
		return null;
	}

	public void insertDataset(EmfDataset aDataset) throws EmfException {
        Session session = HibernateUtils.currentSession();
        DatasetDAO.insertDatasetType(aDataset,session);
		
	}

}
