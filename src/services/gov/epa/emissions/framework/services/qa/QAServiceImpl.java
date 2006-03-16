package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.persistence.DatasetDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

public class QAServiceImpl implements QAService {
    private static Log LOG = LogFactory.getLog(QAServiceImpl.class);

    private HibernateSessionFactory sessionFactory;

    private DatasetDAO dao;

    public QAServiceImpl() {
        this(HibernateSessionFactory.get());
    }

    public QAServiceImpl(HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        dao = new DatasetDAO();
    }

    public QAStep[] getQASteps(EmfDataset dataset) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            QAStep[] results = dao.steps(dataset, session);
            session.close();

            return results;
        } catch (RuntimeException e) {
            LOG.error("could not fetch QA Steps for dataset: " + dataset.getName(), e);
            throw new EmfException("could not fetch QA Steps for dataset: " + dataset.getName());
        }
    }
}
