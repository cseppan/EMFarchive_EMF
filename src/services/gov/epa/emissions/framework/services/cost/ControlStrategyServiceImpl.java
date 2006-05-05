package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;

public class ControlStrategyServiceImpl implements ControlStrategyService {

    private static Log LOG = LogFactory.getLog(ControlStrategyServiceImpl.class);

    private HibernateSessionFactory sessionFactory;

    private ControlStrategyDAO dao;

    public ControlStrategyServiceImpl() {
        this(HibernateSessionFactory.get());
    }

    public ControlStrategyServiceImpl(HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        dao = new ControlStrategyDAO();
    }

    public ControlStrategy[] getControlStrategies() throws EmfException {
        try {
            List cs = dao.all(sessionFactory.getSession());
            return (ControlStrategy[]) cs.toArray(new ControlStrategy[0]);
        } catch (HibernateException e) {
            LOG.error("could not retrieve all control strategies.");
            throw new EmfException("could not retrieve all control strategies.");
        }
    }

    public void addControlStrategy(ControlStrategy element) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            dao.add(element, session);
            session.close();
        } catch (RuntimeException e) {
            LOG.error("Could not add Control Strategy: " + element, e);
            throw new EmfException("Could not add Control Strategy: " + element);
        }
    }

    public ControlStrategy obtainLocked(User owner, ControlStrategy element) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            ControlStrategy locked = dao.obtainLocked(owner, element, session);
            session.close();

            return locked;
        } catch (RuntimeException e) {
            LOG
                    .error("Could not obtain lock for Control Strategy: " + element + " by owner: "
                            + owner.getUsername(), e);
            throw new EmfException("Could not obtain lock for Control Strategy: " + element + " by owner: "
                    + owner.getUsername());
        }
    }

    public ControlStrategy releaseLocked(ControlStrategy locked) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            ControlStrategy released = dao.releaseLocked(locked, session);
            session.close();

            return released;
        } catch (RuntimeException e) {
            LOG.error(
                    "Could not release lock for Control Strategy : " + locked + " by owner: " + locked.getLockOwner(),
                    e);
            throw new EmfException("Could not release lock for Control Strategy: " + locked + " by owner: "
                    + locked.getLockOwner());
        }
    }

    public ControlStrategy updateControlStrategy(ControlStrategy element) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            
            if (!dao.canUpdate(element, session))
                throw new EmfException("Control Strategy name already in use");

            ControlStrategy released = dao.update(element, session);
            session.close();

            return released;
        } catch (RuntimeException e) {
            LOG.error("Could not update Control Strategy: " + element, e);
            throw new EmfException("Could not update ControlStrategy: " + element);
        }
    }

}
