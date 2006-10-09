package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.LockingScheme;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

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

    public void add(ControlStrategyResult element, Session session) {
        addObject(element, session);
    }

    private void addObject(Object obj, Session session) {
        hibernateFacade.add(obj, session);
    }

    // return ControlStrategies orderby name
    public List all(Session session) {
        return hibernateFacade.getAll(ControlStrategy.class, Order.asc("name"), session);
    }

    public List getAllStrategyTypes(Session session) {
        return hibernateFacade.getAll(StrategyType.class, Order.asc("name"), session);
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

    public ControlStrategy updateWithLock(ControlStrategy locked, Session session) throws EmfException {
        return (ControlStrategy) lockingScheme.keepLockOnUpdate(locked, session, all(session));
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

    public void remove(ControlStrategy strategy, Session session) {
        hibernateFacade.remove(strategy, session);
    }

    public StrategyResultType getDetailedStrategyResultType(Session session) {
        List all = hibernateFacade.getAll(StrategyResultType.class, Order.asc("name"), session);
        for (int i = 0; i < all.size(); i++) {
            StrategyResultType type = (StrategyResultType) all.get(i);
            if (type.getName().equals("Detailed Strategy Result"))
                return type;
        }
        return null;
    }

    public ControlStrategyResult controlStrategyResult(ControlStrategy controlStrategy, Session session) {
        updateControlStrategyIds(controlStrategy, session);
        Criterion c1 = Restrictions.eq("controlStrategyId", new Integer(controlStrategy.getId()));
        EmfDataset[] inputDatasets = controlStrategy.getInputDatasets();
        Criterion c2 = Restrictions.eq("detailedResultDataset", inputDatasets[0]);
        Criterion[] criterions = { c1, c2 };
        List list = hibernateFacade.get(ControlStrategyResult.class, criterions, session);
        if (!list.isEmpty())
            return (ControlStrategyResult) list.get(0);
        return null;
    }

    private void updateControlStrategyIds(ControlStrategy controlStrategy, Session session) {
        Criterion c1 = Restrictions.eq("name", controlStrategy.getName());
        List list = hibernateFacade.get(ControlStrategy.class, c1, session);
        if (!list.isEmpty()) {
            ControlStrategy cs = (ControlStrategy) list.get(0);
            controlStrategy.setId(cs.getId());
        }
    }

    public void updateControlStrategyResults(ControlStrategyResult result, Session session) {
        hibernateFacade.update(result, session);
    }
}
