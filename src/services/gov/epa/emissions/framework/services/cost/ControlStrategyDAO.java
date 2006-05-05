package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.LockingScheme;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Order;

public class ControlStrategyDAO {

    private LockingScheme lockingScheme;

    private HibernateFacade hibernateFacade;

    public ControlStrategyDAO() {
        lockingScheme = new LockingScheme();
        hibernateFacade = new HibernateFacade();
    }

    public void add(ControlStrategy element, Session session) {
        addObject(element, session);
    }

    private void addObject(Object obj, Session session) {
        hibernateFacade.add(obj, session);
    }

    // return ControlStrategies orderby name
    public List all(Session session) {
        return hibernateFacade.getAll(ControlStrategy.class, Order.asc("name"), session);
    }

    // TODO: gettig all the strategies to obtain the lock--- is it a good idea?
    public ControlStrategy obtainLocked(User owner, ControlStrategy element, Session session) {
        return (ControlStrategy) lockingScheme.getLocked(owner, element, session, all(session));
    }

    public ControlStrategy releaseLocked(ControlStrategy locked, Session session) {
        return (ControlStrategy) lockingScheme.releaseLock(locked, session, all(session));
    }

    public ControlStrategy update(ControlStrategy locked, Session session) throws EmfException {
        return (ControlStrategy) lockingScheme.releaseLockOnUpdate(locked, session, all(session));
    }

    public boolean canUpdate(ControlStrategy controlStrategy, Session session) {
        if (!exists(controlStrategy.getId(), ControlStrategy.class, session)) {
            return false;
        }

        ControlStrategy current = current(controlStrategy.getId(), ControlStrategy.class, session);

        session.clear();// clear to flush current

        if (current.getName().equals(controlStrategy.getName()))
            return true;

        return !nameUsed(controlStrategy.getName(), ControlStrategy.class, session);
    }

    private boolean nameUsed(String name, Class clazz, Session session) {
        return hibernateFacade.nameUsed(name, clazz, session);
    }

    private ControlStrategy current(int id, Class clazz, Session session) {
        return (ControlStrategy) hibernateFacade.current(id, clazz, session);
    }

    public boolean exists(int id, Class clazz, Session session) {
        return hibernateFacade.exists(id, clazz, session);
    }
}
