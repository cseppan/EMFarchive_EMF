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
import org.hibernate.HibernateException;
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
            List all = dao.all(sessionFactory.getSession());
            return (ControlMeasure[]) all.toArray(new ControlMeasure[0]);
        } catch (HibernateException e) {
            e.printStackTrace();
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

            return released;
        } catch (RuntimeException e) {
            LOG.error("Could not release lock for ControlMeasure: " + locked.getName() + " by owner: "
                    + locked.getLockOwner(), e);
            throw new EmfException("Could not release lock for ControlMeasure: " + locked.getName() + " by owner: "
                    + locked.getLockOwner());
        }
    }

    public ControlMeasure updateMeasure(ControlMeasure measure) throws EmfException {
        return dao.update(measure, sessionFactory.getSession());
    }

    public Scc[] getSccs(ControlMeasure measure) throws EmfException {
        Scc[] sccs = dao.geSccs(measure, sessionFactory.getSession(), dbServer);
        return sccs;
    }

}
