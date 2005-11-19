/*
 * Creation on Oct 21, 2005
 * Eclipse Project Name: EMF
 * File Name: DatasetTypesServicesImpl.java
 * Author: Conrad F. D'Cruz
 */
/**
 * 
 */

package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.dao.DatasetTypesDAO;
import gov.epa.emissions.framework.services.DatasetTypeService;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;

/**
 * @author Conrad F. D'Cruz
 *
 */
public class DatasetTypeServiceImpl implements DatasetTypeService {
    private static Log log = LogFactory.getLog(DatasetTypeServiceImpl.class);

	/**
	 * 
	 */
	public DatasetTypeServiceImpl() {
		super();
	}

	/* (non-Javadoc)
	 * @see gov.epa.emissions.framework.services.DatasetTypesServices#getDatasetTypes()
	 */
	public DatasetType[] getDatasetTypes() throws EmfException {
		
        List datasettypes = null;
		try {
			log.debug("In DatasetTypesServicesImpl:getDatasetTypes START");
			// Session session = HibernateUtils.currentSession();
			Session session = EMFHibernateUtil.getSession();
			datasettypes = DatasetTypesDAO.getDatasetTypes(session);
			log.debug("In DatasetTypesServicesImpl:getDatasetTypes END");
			session.flush();
			session.close();
		} catch (HibernateException e) {
			log.error("Error in the database" + e);
			throw new EmfException("Database error");
		}

        return (DatasetType[]) datasettypes.toArray(new DatasetType[datasettypes.size()]);
	}

	/* (non-Javadoc)
	 * @see gov.epa.emissions.framework.services.DatasetTypesServices#insertDatasetType(gov.epa.emissions.commons.io.DatasetType)
	 */
	public void insertDatasetType(DatasetType datasetType) throws EmfException {
        try {
            Session session = EMFHibernateUtil.getSession();
            DatasetTypesDAO.insertDatasetType(datasetType,session);
            session.flush();
            session.close();
        } catch (HibernateException e) {
            log.error("Database error: " + e);
            throw new EmfException("Error communicating with the server");
        }
	}

	/* (non-Javadoc)
	 * @see gov.epa.emissions.framework.services.DatasetTypesServices#updateDatasetType(gov.epa.emissions.commons.io.DatasetType)
	 */
	public void updateDatasetType(DatasetType datasetType) throws EmfException {
        try {
            Session session = EMFHibernateUtil.getSession();
            DatasetTypesDAO.updateDatasetType(datasetType,session);
            session.flush();
            session.close();
        } catch (HibernateException e) {
            log.error("Database error: " + e);
            throw new EmfException("Error communicating with the server");
        }

	}

}
