package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.cost.data.ControlTechnology;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

public class ControlMeasureServiceImpl implements ControlMeasureService {

    private static Log LOG = LogFactory.getLog(ControlMeasureServiceImpl.class);

    private HibernateSessionFactory sessionFactory;

    private ControlMeasuresDAO dao;
    
    private ControlTechnologiesDAO controlTechnologiesDAO;

    public ControlMeasureServiceImpl() throws Exception {
        this(HibernateSessionFactory.get());
    }

    public ControlMeasureServiceImpl(HibernateSessionFactory sessionFactory) throws Exception {
        init(sessionFactory);
    }

    private void init(HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        dao = new ControlMeasuresDAO();
        controlTechnologiesDAO = new ControlTechnologiesDAO();
    }

    public ControlMeasure[] getMeasures() throws EmfException {
        Session session = null;
        try {
            session = sessionFactory.getSession();
            List all = dao.all(session);

            return (ControlMeasure[]) all.toArray(new ControlMeasure[0]);
        } catch (RuntimeException e) {
            LOG.error("could not retrieve control measures.", e);
            throw new EmfException("could not retrieve control measures.");
        } finally {
            if (session != null)
                session.close();
        }
    }

    public void addMeasure(ControlMeasure measure) throws EmfException {
        Session session = null;
        try {
            session = sessionFactory.getSession();
            dao.add(measure, session);
        } catch (RuntimeException e) {
            LOG.error("Could not add control measure: " + measure.getName(), e);
            throw new EmfException("Could not add control measure: " + measure.getName());
        } finally {
            if (session != null)
                session.close();
        }
    }

    public void removeMeasure(ControlMeasure measure) throws EmfException {
        Session session = null;
        try {
            session = sessionFactory.getSession();
            dao.remove(measure, session);
        } catch (RuntimeException e) {
            LOG.error("Could not remove control measure: " + measure.getName(), e);
            throw new EmfException("Could not remove control measure: " + measure.getName());
        } finally {
            if (session != null)
                session.close();
        }
    }

    public ControlMeasure obtainLockedMeasure(User owner, ControlMeasure measure) throws EmfException {
        Session session = null;
        try {
            session = sessionFactory.getSession();
            ControlMeasure locked = dao.obtainLocked(owner, measure, session);

            return locked;
        } catch (RuntimeException e) {
            LOG.error("Could not obtain lock for ControlMeasure: " + measure.getName() + " by owner: "
                    + owner.getUsername(), e);
            throw new EmfException("Could not obtain lock for ControlMeasure: " + measure.getName() + " by owner: "
                    + owner.getUsername());
        } finally {
            if (session != null)
                session.close();
        }
    }

    public ControlMeasure releaseLockedControlMeasure(ControlMeasure locked) throws EmfException {
        Session session = null;
        try {
            session = sessionFactory.getSession();
            ControlMeasure released = dao.releaseLocked(locked, session);

            return released;
        } catch (RuntimeException e) {
            LOG.error("Could not release lock for ControlMeasure: " + locked.getName() + " by owner: "
                    + locked.getLockOwner(), e);
            throw new EmfException("Could not release lock for ControlMeasure: " + locked.getName() + " by owner: "
                    + locked.getLockOwner());
        } finally {
            if (session != null)
                session.close();
        }
    }

    public ControlMeasure updateMeasure(ControlMeasure measure) throws EmfException {
        Session session = null;
        try {
            session = sessionFactory.getSession();
            ControlMeasure updated = dao.update(measure, session);

            return updated;
        } catch (RuntimeException e) {
            LOG.error("Could not update for ControlMeasure: " + measure.getName(), e);
            throw new EmfException("Could not update for ControlMeasure: " + measure.getName());
        } finally {
            if (session != null)
                session.close();
        }
    }

    public Scc[] getSccs(ControlMeasure measure) throws EmfException {
        try {
            Scc[] sccs = dao.geSccs(measure);
            return sccs;
        } catch (RuntimeException e) {
            LOG.error("Could not get SCCs for ControlMeasure: " + measure.getName(), e);
            throw new EmfException("Could not get SCCs for ControlMeasure: " + measure.getName());
        }
    }

    public ControlTechnology[] getControlTechnologies() throws EmfException {
        Session session = null;
        try {
            session = sessionFactory.getSession();
            List all = controlTechnologiesDAO.getAll(session);

            return (ControlTechnology[]) all.toArray(new ControlTechnology[0]);
        } catch (RuntimeException e) {
            LOG.error("could not retrieve control technologies.", e);
            throw new EmfException("could not retrieve control technologies.");
        } finally {
            if (session != null)
                session.close();
        }
    }

    public void addControlTechnology(ControlTechnology technology) throws EmfException {
        Session session = null;
        try {
            session = sessionFactory.getSession();
            controlTechnologiesDAO.addControlTechnology(technology, session);
        } catch (RuntimeException e) {
            LOG.error("Could not add control technology: " + technology.getName(), e);
            throw new EmfException("Could not add control technology: " + technology.getName());
        } finally {
            if (session != null)
                session.close();
        }
    }
    
    public ControlTechnology updateControlTechnology(ControlTechnology technology, Session session) throws EmfException {
        return controlTechnologiesDAO.update(technology, session);
    }
    
    public ControlTechnology obtainLockedControlTechnology(User user, ControlTechnology technology, Session session) {
        return controlTechnologiesDAO.obtainLocked(user, technology, session);
    }

    public ControlTechnology releaseLockedControlTechnology(ControlTechnology locked, Session session)  {
        return controlTechnologiesDAO.releaseLocked(locked, session);
    }

}
