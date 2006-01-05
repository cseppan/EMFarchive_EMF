package gov.epa.emissions.framework.dao;

import gov.epa.emissions.commons.io.Lockable;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class LockingScheme {
    private static Log log = LogFactory.getLog(LockingScheme.class);

    public static final long DEFAULT_TIMEOUT = 12 * 60 * 60 * 1000;

    /**
     * This method will check if the current sector record has a lock. If it does it will return the sector object with
     * the lock parameters to the current user indicating who is using this object. If the lock is older than 12 hours
     * then the current user will be given the lock.
     * 
     * If there is no lock, this user will grab the lock and a modified record indicating the ownership of the lock is
     * set back to the GUI.
     * 
     * The client will cross check those paramters in the returned sector object against the current user in the GUI. If
     * the user is the same the GUI will allow the user to edit. If not the GUI will switch to view mode and a dialog
     * will display the Full Name of the user who has the lock and the date the lock was acquired.
     * 
     */
    public Lockable getLock(User user, Lockable target, Session session, List all) {
        Lockable current = current(target, all);
        return getLocked(user, session, current);
    }

    private Lockable getLocked(User user, Session session, Lockable current) {
        if (!current.isLocked()) {
            grabLock(user, current, session);
            return current;
        }

        long elapsed = new Date().getTime() - current.getLockDate().getTime();

        if ((user.getFullName().equals(current.getLockOwner())) || (elapsed > DEFAULT_TIMEOUT)) {
            grabLock(user, current, session);
        }

        return current;
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
        lockable.setLockOwner(user.getFullName());
        lockable.setLockDate(new Date());

        Transaction tx = session.beginTransaction();
        try {
            session.update(lockable);
            tx.commit();
        } catch (HibernateException e) {
            log.error(e);
            tx.rollback();
            throw e;
        }
    }

    public Lockable releaseLock(Lockable locked, Session session, List all) throws EmfException {
        Lockable current = current(locked, all);
        return releaseLock(current, session);
    }

    private Lockable releaseLock(Lockable current, Session session) throws EmfException {
        if (!current.isLocked())
            throw new EmfException("Cannot update without owning lock");

        Transaction tx = session.beginTransaction();
        try {
            current.setLockOwner(null);
            current.setLockDate(null);
            session.update(current);

            tx.commit();
        } catch (HibernateException e) {
            log.error(e);
            tx.rollback();
            throw e;
        }

        return current;
    }

    public Lockable update(User user, Lockable target, Session session, List all) throws EmfException {
        Lockable current = current(target, all);
        if (!current.isLocked(user))
            throw new EmfException("Cannot update without owning lock");

        session.clear();// clear 'loaded' locked object - to make way for updated object
        doUpdate(session, target);

        return releaseLock(target, session);
    }

    private void doUpdate(Session session, Lockable target) {
        Transaction tx = session.beginTransaction();
        try {
            target.setLockDate(new Date());
            session.update(target);
            tx.commit();
        } catch (HibernateException e) {
            log.error(e);
            tx.rollback();
            throw e;
        }
    }

}
