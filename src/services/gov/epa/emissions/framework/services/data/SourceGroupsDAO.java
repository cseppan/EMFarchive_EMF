package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.SourceGroup;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.OldLockingScheme;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Order;

public class SourceGroupsDAO {

    private OldLockingScheme lockingScheme;

    private HibernateFacade hibernateFacade;

    public SourceGroupsDAO() {
        lockingScheme = new OldLockingScheme();
        hibernateFacade = new HibernateFacade();
    }

    public List getAll(Session session) {
        return session.createCriteria(SourceGroup.class).addOrder(Order.asc("name")).list();
    }

    public SourceGroup obtainLocked(User user, SourceGroup sourcegrp, Session session) {
        return (SourceGroup) lockingScheme.getLocked(user, sourcegrp, session, getAll(session));
    }

    public SourceGroup update(SourceGroup sourcegrp, Session session) throws EmfException {
        return (SourceGroup) lockingScheme.releaseLockOnUpdate(sourcegrp, session, getAll(session));
    }

    public SourceGroup releaseLocked(SourceGroup locked, Session session)  {
        return (SourceGroup) lockingScheme.releaseLock(locked, session, getAll(session));
    }

    /*
     * True if sourcegrp exists in database
     * 
     * 1. Should Exist 2. Your id matches existing Id 3. Your name should not match another object's name
     * 
     */
    public boolean canUpdate(SourceGroup sourcegrp, Session session) {
        if (!exists(sourcegrp.getId(), SourceGroup.class, session)) {
            return false;
        }

        SourceGroup current = current(sourcegrp.getId(), SourceGroup.class, session);
        // The current object is saved in the session. Hibernate cannot persist our
        // object with the same id.
        session.clear();
        if (current.getName().equals(sourcegrp.getName()))
            return true;

        return !nameUsed(sourcegrp.getName(), SourceGroup.class, session);
    }

    private boolean nameUsed(String name, Class clazz, Session session) {
        return hibernateFacade.nameUsed(name, clazz, session);
    }

    private SourceGroup current(int id, Class clazz, Session session) {
        return (SourceGroup) hibernateFacade.current(id, clazz, session);
    }

    private boolean exists(int id, Class clazz, Session session) {
        return hibernateFacade.exists(id, clazz, session);
    }

}
