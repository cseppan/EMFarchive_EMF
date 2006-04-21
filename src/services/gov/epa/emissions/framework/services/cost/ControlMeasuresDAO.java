package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.LockingScheme;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class ControlMeasuresDAO {

    private LockingScheme lockingScheme;

    private HibernateFacade hibernateFacade;

    public ControlMeasuresDAO() {
        lockingScheme = new LockingScheme();
        hibernateFacade = new HibernateFacade();
    }

    public boolean exists(int id, Class clazz, Session session) {
        return hibernateFacade.exists(id, clazz, session);
    }

    /*
     * Return true if the name is already used
     */
    public boolean nameUsed(String name, Class clazz, Session session) {
        return hibernateFacade.nameUsed(name, clazz, session);
    }

    public ControlMeasure current(int id, Class clazz, Session session) {
        return (ControlMeasure) hibernateFacade.current(id, clazz, session);
    }

    public boolean canUpdate(ControlMeasure measure, Session session) {
        if (!exists(measure.getId(), EmfDataset.class, session)) {
            return false;
        }

        ControlMeasure current = current(measure.getId(), ControlMeasure.class, session);
        session.clear();// clear to flush current
        if (current.getName().equals(measure.getName()))
            return true;

        return !nameUsed(measure.getName(), ControlMeasure.class, session);
    }

    public boolean exists(String name, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(ControlMeasure.class).add(Restrictions.eq("name", name));
            tx.commit();

            return crit.uniqueResult() != null;
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public List all(Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            List all = session.createCriteria(ControlMeasure.class).addOrder(Order.asc("name")).list();
            tx.commit();

            return all;
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public void add(ControlMeasure measure, Session session) {
        hibernateFacade.add(measure, session);
    }

    public void updateWithoutLocking(ControlMeasure measure, Session session) {
        hibernateFacade.update(measure, session);
    }

    public void remove(ControlMeasure measure, Session session) {
        hibernateFacade.remove(measure, session);
    }

    public ControlMeasure obtainLocked(User user, ControlMeasure measure, Session session) {
        return (ControlMeasure) lockingScheme.getLocked(user, measure, session, all(session));
    }

    public ControlMeasure releaseLocked(ControlMeasure locked, Session session) {
        return (ControlMeasure) lockingScheme.releaseLock(locked, session, all(session));
    }

    public ControlMeasure update(ControlMeasure locked, Session session) throws EmfException {
        return (ControlMeasure) lockingScheme.releaseLockOnUpdate(locked, session, all(session));
    }

    public void update(ControlMeasure[] measures, Session session) {
        hibernateFacade.update(measures, session);
    }

    public void add(ControlMeasure[] measures, Session session) {
        hibernateFacade.add(measures, session);
    }

}
