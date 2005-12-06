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

public class DatasetTypeServiceImpl implements DatasetTypeService {
    private static Log log = LogFactory.getLog(DatasetTypeServiceImpl.class);

    public DatasetType[] getDatasetTypes() throws EmfException {

        List datasettypes = null;
        try {
            Session session = EMFHibernateUtil.getSession();
            datasettypes = DatasetTypesDAO.getDatasetTypes(session);

            close(session);
        } catch (HibernateException e) {
            log.error("Error in the database" + e);
            throw new EmfException("Database error");
        }

        return (DatasetType[]) datasettypes.toArray(new DatasetType[datasettypes.size()]);
    }

    public void insertDatasetType(DatasetType datasetType) throws EmfException {
        try {
            Session session = EMFHibernateUtil.getSession();
            DatasetTypesDAO.insertDatasetType(datasetType, session);

            close(session);
        } catch (HibernateException e) {
            log.error("Database error: " + e);
            throw new EmfException("Error communicating with the server");
        }
    }

    public void updateDatasetType(DatasetType datasetType) throws EmfException {
        try {
            Session session = EMFHibernateUtil.getSession();
            DatasetTypesDAO.updateDatasetType(datasetType, session);

            close(session);
        } catch (HibernateException e) {
            log.error("Database error: " + e);
            throw new EmfException("Error communicating with the server");
        }

    }

    private void close(Session session) {
        session.flush();
        session.close();
    }

}
