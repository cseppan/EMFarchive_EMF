package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyConstraint;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.LockingScheme;

import java.util.ArrayList;
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

    public int add(ControlStrategy element, Session session) {
        return addObject(element, session);
    }

    public void add(ControlStrategyConstraint element, Session session) {
        addObject(element, session);
    }

    public void add(ControlStrategyResult element, Session session) {
        addObject(element, session);
    }

    private int addObject(Object obj, Session session) {
        return (Integer)hibernateFacade.add(obj, session);
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
        return (ControlStrategy) lockingScheme.getLocked(owner, current(element, session), session);
    }

//    public void releaseLocked(ControlStrategy locked, Session session) {
//        ControlStrategy current = current(locked, session);
//        String runStatus = current.getRunStatus();
//        if (runStatus == null || !runStatus.equalsIgnoreCase("Running"))
//            lockingScheme.releaseLock(current, session);
//    }

    public void releaseLocked(int id, Session session) {
        ControlStrategy current = getById(id, session);
        String runStatus = current.getRunStatus();
        if (runStatus == null || !runStatus.equalsIgnoreCase("Running"))
            lockingScheme.releaseLock(current, session);
    }

    public ControlStrategy update(ControlStrategy locked, Session session) throws EmfException {
        return (ControlStrategy) lockingScheme.releaseLockOnUpdate(locked, current(locked, session), session);
    }

    public ControlStrategy updateWithLock(ControlStrategy locked, Session session) throws EmfException {
        return (ControlStrategy) lockingScheme.renewLockOnUpdate(locked, current(locked, session), session);
    }

    private ControlStrategy current(ControlStrategy strategy, Session session) {
        return current(strategy.getId(), ControlStrategy.class, session);
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

    public boolean nameUsed(String name, Class clazz, Session session) {
        return hibernateFacade.nameUsed(name, clazz, session);
    }

    private ControlStrategy current(int id, Class clazz, Session session) {
        return (ControlStrategy) hibernateFacade.current(id, clazz, session);
    }

    public boolean exists(int id, Class clazz, Session session) {
        return hibernateFacade.exists(id, clazz, session);
    }

    public void remove(ControlStrategy strategy, Session session) {
        if (strategy.getConstraint() != null) hibernateFacade.remove(strategy.getConstraint(), session);
        hibernateFacade.remove(strategy, session);
    }

    public void remove(ControlStrategyResult result, Session session) {
        hibernateFacade.remove(result, session);
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

        List criterions = new ArrayList();
        Criterion c1 = Restrictions.eq("controlStrategyId", new Integer(controlStrategy.getId()));
        criterions.add(c1);

        EmfDataset[] inputDatasets = controlStrategy.getInputDatasets();
//        System.err.println(inputDatasets.length);
        if (inputDatasets.length != 0) {
//            System.err.println(inputDatasets[0] == null);
            Criterion c2 = Restrictions.eq("inputDatasetId", new Integer(inputDatasets[0].getId()));
            criterions.add(c2);
        }

        List list = hibernateFacade.get(ControlStrategyResult.class,
                (Criterion[]) criterions.toArray(new Criterion[0]), session);
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
        hibernateFacade.saveOrUpdate(result, session);
    }

    public String controlStrategyRunStatus(int id, Session session) {
        ControlStrategy controlStrategy = (ControlStrategy) hibernateFacade.current(id, ControlStrategy.class, session);
        return controlStrategy.getRunStatus();
    }

    public void removeControlStrategyResult(ControlStrategy controlStrategy, Session session) {
        Criterion c = Restrictions.eq("controlStrategyId", new Integer(controlStrategy.getId()));
        List list = hibernateFacade.get(ControlStrategyResult.class, c, session);
        for (int i = 0; i < list.size(); i++) {
            ControlStrategyResult result = (ControlStrategyResult) list.get(i);
            hibernateFacade.delete(result,session);
        }
    }

    public ControlStrategy getByName(String name, Session session) {
        ControlStrategy cs = (ControlStrategy) hibernateFacade.load(ControlStrategy.class, Restrictions.eq("name", new String(name)), session);
        return cs;
    }

    public ControlStrategy getById(int id, Session session) {
        ControlStrategy cs = (ControlStrategy) hibernateFacade.load(ControlStrategy.class, Restrictions.eq("id", new Integer(id)), session);
        return cs;
    }
}
