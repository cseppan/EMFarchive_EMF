package gov.epa.emissions.framework.services.persistence;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Order;

public class SectorsDAO {

    private LockingScheme lockingScheme;

    private HibernateFacade hibernateFacade;

    public SectorsDAO() {
        lockingScheme = new LockingScheme();
        hibernateFacade = new HibernateFacade();
    }

    public List getAll(Session session) {
        return session.createCriteria(Sector.class).addOrder(Order.asc("name")).list();
    }

    public Sector obtainLocked(User user, Sector sector, Session session) {
        return (Sector) lockingScheme.getLocked(user, sector, session, getAll(session));
    }

    public Sector update(Sector sector, Session session) throws EmfException {
        return (Sector) lockingScheme.releaseLockOnUpdate(sector, session, getAll(session));
    }

    public Sector releaseLocked(Sector locked, Session session) throws EmfException {
        return (Sector) lockingScheme.releaseLock(locked, session, getAll(session));
    }

    /*
     * True if sector exists in database
     * 
     * 1. Should Exist 2. Your id matches existing Id 3. Your name should not match another object's name
     * 
     */
    public boolean canUpdate(Sector sector, Session session) {
        if (!exists(sector.getId(), Sector.class, session)) {
            return false;
        }

        Sector current = current(sector.getId(), Sector.class, session);
        // The current object is saved in the session. Hibernate cannot persist our
        // object with the same id.
        session.clear();
        if (current.getName().equals(sector.getName()))
            return true;

        return !nameUsed(sector.getName(), Sector.class, session);
    }

    private boolean nameUsed(String name, Class clazz, Session session) {
        return hibernateFacade.nameUsed(name, clazz, session);
    }

    private Sector current(int id, Class clazz, Session session) {
        return (Sector) hibernateFacade.current(id, clazz, session);
    }

    private boolean exists(int id, Class clazz, Session session) {
        return hibernateFacade.exists(id, clazz, session);
    }

}
