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
    public List getControlStrategies(Session session) {
        return session.createCriteria(ControlStrategy.class).addOrder(Order.asc("name")).list();
    }

    // TODO: gettig all the strategies to obtain the lock--- is it a good idea?
    public ControlStrategy obtainLocked(User owner, ControlStrategy element, Session session) {
        return (ControlStrategy) lockingScheme.getLocked(owner, element, session, getControlStrategies(session));
    }

    public ControlStrategy releaseLocked(ControlStrategy locked, Session session) {
        return (ControlStrategy) lockingScheme.releaseLock(locked, session, getControlStrategies(session));
    }

    public ControlStrategy update(ControlStrategy locked, Session session) throws EmfException {
        return (ControlStrategy) lockingScheme.releaseLockOnUpdate(locked, session, getControlStrategies(session));
    }

}
