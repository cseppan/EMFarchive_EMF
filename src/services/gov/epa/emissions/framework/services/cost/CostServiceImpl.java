package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.services.qa.QAServiceImpl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;

public class CostServiceImpl implements CostService {

    private static Log LOG = LogFactory.getLog(QAServiceImpl.class);

    private HibernateSessionFactory sessionFactory;
    
    private ControlMeasuresDAO dao;
    
    public CostServiceImpl() {
        this(HibernateSessionFactory.get());
    }
    
    public CostServiceImpl(HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        dao = new ControlMeasuresDAO();
    }

    public ControlMeasure[] getMeasures() throws EmfException {
        try {
            List all = dao.all(sessionFactory.getSession());
            return (ControlMeasure[]) all.toArray(new ControlMeasure[0]);
        } catch (HibernateException e) {
            LOG.error("could not retrieve control measures.");
            throw new EmfException("could not retrieve control measures.");
        }
    }

    public void addMeasure(ControlMeasure measure) {
        dao.add(measure, sessionFactory.getSession());
    }

    public void removeMeasure(ControlMeasure measure) {
        dao.remove(measure, sessionFactory.getSession());
    }
    
    public ControlMeasure obtainLockedMeasure(User owner, ControlMeasure measure) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            ControlMeasure locked = dao.obtainLocked(owner, measure, session);
            session.close();

            return locked;
        } catch (RuntimeException e) {
            LOG.error("Could not obtain lock for ControlMeasure: " + measure.getName() + " by owner: " + owner.getUsername(),
                    e);
            throw new EmfException("Could not obtain lock for ControlMeasure: " + measure.getName() + " by owner: "
                    + owner.getUsername());
        }
    }

}
