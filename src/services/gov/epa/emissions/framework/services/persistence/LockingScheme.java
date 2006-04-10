package gov.epa.emissions.framework.services.persistence;

import gov.epa.emissions.commons.data.Lockable;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.EmfProperties;
import gov.epa.emissions.framework.services.EmfProperty;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class LockingScheme {
    private EmfProperties propertiesDao;

    public LockingScheme() {
        propertiesDao = new EmfPropertiesDAO();
    }

    public Lockable getLocked(User user, Lockable target, Session session, List all) {
        Lockable current = current(target, all);
        return getLocked(user, current, session);
    }

    private Lockable getLocked(User user, Lockable current, Session session) {
        if (!current.isLocked()) {
            grabLock(user, current, session);
            return current;
        }

        long elapsed = new Date().getTime() - current.getLockDate().getTime();

        if ((user.getName().equals(current.getLockOwner())) || (elapsed > timeInterval(session))) {
            grabLock(user, current, session);
        }

        return current;
    }

    public long timeInterval(Session session) {
        EmfProperty timeInterval = propertiesDao.getProperty("lock.time-interval", session);
        return Long.parseLong(timeInterval.getValue());
    }

    private Lockable current(Lockable target, List list) {
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            Lockable element = (Lockable) iter.next();
            if (element.equals(target))
                return element;
        }

        return null;
    }

    private void grabLock(User user, Lockable lockable, Session session) {
        lockable.setLockOwner(user.getUsername());
        lockable.setLockDate(new Date());

        Transaction tx = session.beginTransaction();
        try {
            session.update(lockable);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public Lockable releaseLock(Lockable locked, Session session, List all) {
        Lockable current = current(locked, all);
        return releaseLock(current, session);
    }

    private Lockable releaseLock(Lockable current, Session session) {
        if (!current.isLocked())
            return current;// abort

        Transaction tx = session.beginTransaction();
        try {
            current.setLockOwner(null);
            current.setLockDate(null);
            session.update(current);

            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }

        return current;
    }

    public Lockable releaseLockOnUpdate(Lockable target, Session session, List all) throws EmfException {
        doUpdate(target, session, all);
        return releaseLock(target, session);
    }

    private void doUpdate(Lockable target, Session session, List all) throws EmfException {
        Lockable current = current(target, all);
        if (!current.isLocked(target.getLockOwner()))
            throw new EmfException("Cannot update without owning lock");

        session.clear();// clear 'loaded' locked object - to make way for updated object
        doUpdate(session, target);
    }

    private void doUpdate(Session session, Lockable target) {
        Transaction tx = session.beginTransaction();
        try {
            target.setLockDate(new Date());
            session.update(target);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public Lockable renewLockOnUpdate(Lockable target, Session session, List all) throws EmfException {
        doUpdate(target, session, all);
        return target;
    }

}
