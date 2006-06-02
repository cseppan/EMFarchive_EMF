package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.EmfServiceImpl;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

public class CostServiceImpl extends EmfServiceImpl implements CostService {

    private static Log LOG = LogFactory.getLog(CostServiceImpl.class);

    private HibernateSessionFactory sessionFactory;

    private ControlMeasuresDAO dao;

    public CostServiceImpl() throws Exception {
        this(HibernateSessionFactory.get());
    }

    public CostServiceImpl(HibernateSessionFactory sessionFactory) throws Exception {
        super("Control Measure Service");
        init(sessionFactory);
    }

    public CostServiceImpl(DataSource source, DbServer server, HibernateSessionFactory sessionFactory) {
        super(source, server);
        init(sessionFactory);

    }

    private void init(HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        dao = new ControlMeasuresDAO();
    }

    public ControlMeasure[] getMeasures() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List all = dao.all(session);
            session.close();
            session.disconnect();

            return (ControlMeasure[]) all.toArray(new ControlMeasure[0]);
        } catch (RuntimeException e) {
            LOG.error("could not retrieve control measures.", e);
            throw new EmfException("could not retrieve control measures.");
        }
    }

    public void addMeasure(ControlMeasure measure) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            dao.add(measure, session);
            session.close();
            session.disconnect();
        } catch (RuntimeException e) {
            LOG.error("Could not add control measure: " + measure.getName(), e);
            throw new EmfException("Could not add control measure: " + measure.getName());
        }
    }

    public void removeMeasure(ControlMeasure measure) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            dao.remove(measure, session);
            session.close();
            session.disconnect();
        } catch (RuntimeException e) {
            LOG.error("Could not remove control measure: " + measure.getName(), e);
            throw new EmfException("Could not remove control measure: " + measure.getName());
        }
    }

    public ControlMeasure obtainLockedMeasure(User owner, ControlMeasure measure) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            ControlMeasure locked = dao.obtainLocked(owner, measure, session);
            session.close();
            session.disconnect();

            return locked;
        } catch (RuntimeException e) {
            LOG.error("Could not obtain lock for ControlMeasure: " + measure.getName() + " by owner: "
                    + owner.getUsername(), e);
            throw new EmfException("Could not obtain lock for ControlMeasure: " + measure.getName() + " by owner: "
                    + owner.getUsername());
        }
    }

    public ControlMeasure releaseLockedControlMeasure(ControlMeasure locked) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            ControlMeasure released = dao.releaseLocked(locked, session);
            session.close();
            session.disconnect();

            return released;
        } catch (RuntimeException e) {
            LOG.error("Could not release lock for ControlMeasure: " + locked.getName() + " by owner: "
                    + locked.getLockOwner(), e);
            throw new EmfException("Could not release lock for ControlMeasure: " + locked.getName() + " by owner: "
                    + locked.getLockOwner());
        }
    }

    public ControlMeasure updateMeasure(ControlMeasure measure) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            ControlMeasure updated = dao.update(measure, session);
            session.close();
            session.disconnect();

            return updated;
        } catch (RuntimeException e) {
            LOG.error("Could not update for ControlMeasure: " + measure.getName(), e);
            throw new EmfException("Could not update for ControlMeasure: " + measure.getName());
        }
    }

    public Scc[] getSccs(ControlMeasure measure) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            Scc[] sccs = dao.geSccs(measure, session, dbServer);
            session.close();
            session.disconnect();

            return sccs;
        } catch (RuntimeException e) {
            LOG.error("Could not get SCCs for ControlMeasure: " + measure.getName(), e);
            throw new EmfException("Could not get SCCs for ControlMeasure: " + measure.getName());
        }
    }

}
