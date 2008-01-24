package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyConstraint;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
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

    public int add(ControlStrategy element, Session session) {
        return addObject(element, session);
    }

    public void add(ControlStrategyConstraint element, Session session) {
        addObject(element, session);
    }

    public int add(ControlStrategyResult element, Session session) {
        return addObject(element, session);
    }

    private int addObject(Object obj, Session session) {
        return (Integer)hibernateFacade.add(obj, session);
    }

    // return ControlStrategies orderby name
    public List all(Session session) {

//        "Name", "Last Modified", "Run Status", "Region", 
//        "Target Pollutant", "Total Cost", "Reduction", 
//        "Project", "Strategy Type", "Cost Year", 
//        "Inv. Year", "Creator"
//        element.getName(), format(element.getLastModifiedDate()), element.getRunStatus(), region(element),
//        element.getTargetPollutant(), getTotalCost(element.getId()), getReduction(element.getId()), 
//        project(element), analysisType(element), costYear(element), 
//        "" + (element.getInventoryYear() != 0 ? element.getInventoryYear() : ""), 
//        element.getCreator().getName()
        return session.createQuery("select new ControlStrategy(cS.id, cS.name, " +
                "cS.lastModifiedDate, cS.runStatus, " +
                "cS.region, cS.targetPollutant, " +
                "cS.project, cS.strategyType, " +
                "cS.costYear, cS.inventoryYear, " +
                "cS.creator, (select sum(sR.totalCost) from ControlStrategyResult sR where sR.controlStrategyId = cS.id), (select sum(sR.totalReduction) from ControlStrategyResult sR where sR.controlStrategyId = cS.id)) from ControlStrategy cS left join cS.strategyType left join cS.region left join cS.project left join cS.region order by cS.name").list();
        //return hibernateFacade.getAll(ControlStrategy.class, Order.asc("name"), session);
    }

    public List getAllStrategyTypes(Session session) {
        return hibernateFacade.getAll(StrategyType.class, Order.asc("name"), session);
    }

//    // TODO: gettig all the strategies to obtain the lock--- is it a good idea?
//    public ControlStrategy obtainLocked(User owner, ControlStrategy element, Session session) {
//        return (ControlStrategy) lockingScheme.getLocked(owner, current(element, session), session);
//    }
//
    public ControlStrategy obtainLocked(User owner, int id, Session session) {
        return (ControlStrategy) lockingScheme.getLocked(owner, current(id, ControlStrategy.class, session), session);
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
        return getStrategyResultType("Detailed Strategy Result", session);
    }

    public StrategyResultType getStrategyResultType(String name, Session session) {
        Criterion critName = Restrictions.eq("name", name);
        return (StrategyResultType)hibernateFacade.load(StrategyResultType.class, critName, session);
    }

    public StrategyResultType getSummaryStrategyResultType(Session session) {
        return getStrategyResultType("Strategy Summary", session);
    }

    public ControlStrategyResult getControlStrategyResult(int controlStrategyId, int inputDatasetId, 
            int detailedResultDatasetId, Session session) {
        Criterion critControlStrategyId = Restrictions.eq("controlStrategyId", controlStrategyId);
        Criterion critInputDatasetId = Restrictions.eq("inputDatasetId", inputDatasetId);
        Criterion critDetailedResultDatasetId = Restrictions.eq("detailedResultDataset.id", detailedResultDatasetId);
        return (ControlStrategyResult)hibernateFacade.load(ControlStrategyResult.class, new Criterion[] {critControlStrategyId, critInputDatasetId, critDetailedResultDatasetId}, 
                session);
    }

    public ControlStrategyResult getControlStrategyResult(int id, Session session) {
        Criterion critId = Restrictions.eq("id", id);
        return (ControlStrategyResult)hibernateFacade.load(ControlStrategyResult.class, new Criterion[] {critId}, 
                session);
    }

//    private void updateControlStrategyIds(ControlStrategy controlStrategy, Session session) {
//        Criterion c1 = Restrictions.eq("name", controlStrategy.getName());
//        List list = hibernateFacade.get(ControlStrategy.class, c1, session);
//        if (!list.isEmpty()) {
//            ControlStrategy cs = (ControlStrategy) list.get(0);
//            controlStrategy.setId(cs.getId());
//        }
//    }
//
    public void updateControlStrategyResult(ControlStrategyResult result, Session session) {
        hibernateFacade.saveOrUpdate(result, session);
    }

    public String controlStrategyRunStatus(int id, Session session) {
        ControlStrategy controlStrategy = (ControlStrategy) hibernateFacade.current(id, ControlStrategy.class, session);
        return controlStrategy.getRunStatus();
    }

//    public void removeControlStrategyResult(ControlStrategy controlStrategy, Session session) {
//        Criterion c = Restrictions.eq("controlStrategyId", new Integer(controlStrategy.getId()));
//        List list = hibernateFacade.get(ControlStrategyResult.class, c, session);
//        for (int i = 0; i < list.size(); i++) {
//            ControlStrategyResult result = (ControlStrategyResult) list.get(i);
//            hibernateFacade.delete(result,session);
//        }
//    }

    public void removeControlStrategyResults(int controlStrategyId, Session session) {
        String hqlDelete = "delete ControlStrategyResult sr where sr.controlStrategyId = :controlStrategyId";
        session.createQuery( hqlDelete )
             .setInteger("controlStrategyId", controlStrategyId)
             .executeUpdate();
        session.flush();
    }

    public ControlStrategy getByName(String name, Session session) {
        ControlStrategy cs = (ControlStrategy) hibernateFacade.load(ControlStrategy.class, Restrictions.eq("name", new String(name)), session);
        return cs;
    }

    public ControlStrategy getById(int id, Session session) {
        ControlStrategy cs = (ControlStrategy) hibernateFacade.load(ControlStrategy.class, Restrictions.eq("id", new Integer(id)), session);
        return cs;
    }

    public List getControlStrategyResults(int controlStrategyId, Session session) {
        Criterion c = Restrictions.eq("controlStrategyId", controlStrategyId);
        return hibernateFacade.get(ControlStrategyResult.class, c, session);
    }
}
