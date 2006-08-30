package gov.epa.emissions.framework.services.cost.controlmeasure;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.cost.controlmeasure.io.CMImportTask;
import gov.epa.emissions.framework.services.data.DataCommonsDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

public class ControlMeasureImportServiceImpl implements ControlMeasureImportService {

    private static Log LOG = LogFactory.getLog(ControlMeasureImportServiceImpl.class);

    private HibernateSessionFactory sessionFactory;

    private DataCommonsDAO dataCommonsDAO;

    public ControlMeasureImportServiceImpl() throws Exception {
        this(HibernateSessionFactory.get());
    }

    public ControlMeasureImportServiceImpl(HibernateSessionFactory sessionFactory) throws Exception {
        this.sessionFactory = sessionFactory;
        this.dataCommonsDAO = new DataCommonsDAO();
    }

    public void importControlMeasures(String folderPath, String[] fileNames, User user) throws EmfException {
        try {
            CMImportTask importTask = new CMImportTask(new File(folderPath), fileNames, user, sessionFactory);
            importTask.run();
        } catch (RuntimeException e) {
            LOG.error("Could not import control measures.", e);
            throw new EmfException("Could not import control measures: " + e.getMessage());
        }
    }

    public Status[] getImportStatus(User user) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List controlMeasureImportStatuses = dataCommonsDAO.getControlMeasureImportStatuses(user.getUsername(),
                    session);
            return (Status[]) controlMeasureImportStatuses.toArray(new Status[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get detail import status messages.", e);
            throw new EmfException("Could not get detail import status messages. " + e.getMessage());
        } finally {
            session.clear();
        }
    }

}
