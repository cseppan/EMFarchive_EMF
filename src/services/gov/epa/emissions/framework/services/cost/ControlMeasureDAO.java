package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfDbServer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.LockingScheme;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class ControlMeasureDAO {

    private LockingScheme lockingScheme;

    private HibernateFacade hibernateFacade;

    public ControlMeasureDAO() {
        lockingScheme = new LockingScheme();
        hibernateFacade = new HibernateFacade();
    }

    public boolean exists(int id, Class clazz, Session session) {
        return hibernateFacade.exists(id, clazz, session);
    }

    public boolean exists(Class clazz, Criterion[] criterions, Session session) {
        return hibernateFacade.exists(clazz, criterions, session);
    }

    public ControlMeasure current(int id, Session session) {
        return (ControlMeasure) hibernateFacade.current(id, ControlMeasure.class, session);
    }

    public boolean canUpdate(ControlMeasure measure, Session session) {
        if (!exists(measure.getId(), ControlMeasure.class, session)) {
            return false;
        }

        ControlMeasure current = current(measure.getId(), session);
        session.clear();// clear to flush current
        if (current.getName().equals(measure.getName()))
            return true;

        return !nameUsed(measure.getName(), ControlMeasure.class, session);
    }

    /*
     * Return true if the name is already used
     */
    private boolean nameUsed(String name, Class clazz, Session session) {
        return hibernateFacade.nameUsed(name, clazz, session);
    }

    public boolean exists(String name, Session session) {
        return hibernateFacade.exists(name, ControlMeasure.class, session);
    }

    public List all(Session session) {
        return hibernateFacade.getAll(ControlMeasure.class, session);
    }

    public List getControlMeasures(Pollutant poll, Session session) {
        Criterion c = Restrictions.eq("majorPollutant", poll);
        return hibernateFacade.get(ControlMeasure.class, c, session);
    }

    // NOTE: it't not happening in one transaction. modify?
    public int add(ControlMeasure measure, Scc[] sccs, Session session) throws EmfException {
        checkForConstraints(measure, session);
        hibernateFacade.add(measure, session);
        int cmId = controlMeasureIds(measure, sccs, session);
        hibernateFacade.add(sccs, session);
        return cmId;
    }

    private int controlMeasureIds(ControlMeasure measure, Scc[] sccs, Session session) {
        ControlMeasure cm = (ControlMeasure) hibernateFacade.load(ControlMeasure.class, Restrictions.eq("name", measure
                .getName()), session);
        int cmId = cm.getId();
        for (int i = 0; i < sccs.length; i++) {
            sccs[i].setControlMeasureId(cmId);
        }
        return cmId;
    }

    public void remove(int controlMeasureId, Session session) {
        removeSccs(controlMeasureId, session);
        hibernateFacade.remove(current(controlMeasureId, session), session);
    }

    private void removeSccs(int controlMeasureId, Session session) {
        Scc[] sccs = getSccs(controlMeasureId, session);
        for (int i = 0; i < sccs.length; i++) {
            hibernateFacade.remove(sccs[i], session);
        }
    }

    public ControlMeasure obtainLocked(User user, int controlMeasureId, Session session) {
        return (ControlMeasure) lockingScheme.getLocked(user, current(controlMeasureId, session), session);
    }

//    private ControlMeasure current(ControlMeasure measure, Session session) {
//        return current(measure.getId(), ControlMeasure.class, session);
//    }

    public void releaseLocked(int controlMeasureId, Session session) {
        ControlMeasure cm = current(controlMeasureId, session);
        lockingScheme.releaseLock(cm, session);
    }

    public ControlMeasure update(ControlMeasure locked, Scc[] sccs, Session session) throws EmfException {
        checkForConstraints(locked, session);

        ControlMeasure releaseLockOnUpdate = (ControlMeasure) lockingScheme.releaseLockOnUpdate(locked, current(locked.getId(),
                session), session);
        updateSccs(sccs, locked.getId(), session);
        return releaseLockOnUpdate;
    }

    private void updateSccs(Scc[] sccs, int controlMeasureId, Session session) {
        updateSccsControlMeasureIds(sccs, controlMeasureId);
        Scc[] existingSccs = getSccs(controlMeasureId, session);
        List removeList = new ArrayList(Arrays.asList(existingSccs));
        List newSccList = new ArrayList();
        processSccsList(sccs, newSccList, removeList);

        hibernateFacade.remove(removeList.toArray(new Scc[0]), session);
        hibernateFacade.add(newSccList.toArray(new Scc[0]), session);

    }

    // initally all existing sccs in the removeScc list
    private void processSccsList(Scc[] sccsFromClient, List newSccsList, List removeSccs) {
        for (int i = 0; i < sccsFromClient.length; i++) {
            int index = removeSccs.indexOf(sccsFromClient[i]);
            if (index != -1) {
                removeSccs.remove(index);
            } else {
                newSccsList.add(sccsFromClient[i]);
            }
        }
    }

    private void updateSccsControlMeasureIds(Scc[] sccs, int controlMeasureId) {
        for (int i = 0; i < sccs.length; i++) {
            sccs[i].setControlMeasureId(controlMeasureId);
        }
    }

    public Scc[] getSccsWithDescriptions(int controlMeasureId) throws EmfException {
        try {
            RetrieveSCC retrieveSCC = new RetrieveSCC(controlMeasureId, new EmfDbServer());
            return retrieveSCC.sccs();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    public Scc[] getSccs(int controlMeasureId, Session session) {
        Criterion id = Restrictions.eq("controlMeasureId", new Integer(controlMeasureId));
        List list = hibernateFacade.get(Scc.class, new Criterion[] { id }, session);
        return (Scc[]) list.toArray(new Scc[0]);
    }

    public String[] getCMAbbrevAndSccs(int measureId) throws EmfException {
        try {
            RetrieveSCC retrieveSCC = new RetrieveSCC(measureId, new EmfDbServer());
            return retrieveSCC.cmAbbrevAndSccs();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    public void checkForConstraints(ControlMeasure controlMeasure, Session session) throws EmfException {
        Criterion id = Restrictions.ne("id", new Integer(controlMeasure.getId()));
        Criterion name = Restrictions.eq("name", controlMeasure.getName());
        Criterion abbrev = Restrictions.eq("abbreviation", controlMeasure.getAbbreviation());

        if (nameExist(new Criterion[] { id, name }, session))
            throw new EmfException("The Control Measure name is already in use: " + controlMeasure.getName());

        if (abbrExist(new Criterion[] { id, abbrev }, session))
            throw new EmfException("The Control Measure Abbreviation already in use: "
                    + controlMeasure.getAbbreviation());
    }

    private boolean nameExist(Criterion[] criterions, Session session) {
        return exists(ControlMeasure.class, criterions, session);
    }

    private boolean abbrExist(Criterion[] criterions, Session session) {
        return exists(ControlMeasure.class, criterions, session);
    }

    public ControlMeasure load(ControlMeasure measure, Session session) {
        return (ControlMeasure) hibernateFacade.load(ControlMeasure.class, Restrictions.eq("name", measure.getName()),
                session);
    }

    public void addFromImporter(ControlMeasure measure, Scc[] sccs, User user, Session session) throws EmfException {
        Criterion name = Restrictions.eq("name", measure.getName());
        Criterion abbrev = Restrictions.eq("abbreviation", measure.getAbbreviation());
        boolean abbrExist = abbrExist(new Criterion[] { abbrev }, session);
        boolean nameExist = nameExist(new Criterion[] { name }, session);

        if (nameExist) {// overwrite if name exist regard less of abbrev
            int id = controlMeasureId(measure, session);
            measure.setId(id);
            ControlMeasure obtainLocked = obtainLocked(user, measure.getId(), session);
            measure.setLockDate(obtainLocked.getLockDate());
            measure.setLockOwner(obtainLocked.getLockOwner());
            if (obtainLocked == null)
                throw new EmfException("Could not obtain the lock to update: " + measure.getName());
            removeSccs(measure.getId(), session);
            removeEfficiencyRecord(measure, session);
            update(measure, sccs, session);
        } else if (abbrExist) {
            throw new EmfException("The Control Measure Abbreviation already in use: " + measure.getAbbreviation());
        } else {
            add(measure, sccs, session);
        }
    }

    // use only after confirming measure is exist
    private int controlMeasureId(ControlMeasure measure, Session session) {
        Criterion criterion = Restrictions.eq("name", measure.getName());

        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria criteria = session.createCriteria(ControlMeasure.class);
            criteria.add(criterion);
            tx.commit();
            return ((ControlMeasure) criteria.uniqueResult()).getId();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    private void removeEfficiencyRecord(ControlMeasure measure, Session session) {
        Criterion c = Restrictions.eq("controlMeasureId", new Integer(measure.getId()));
        List list = hibernateFacade.get(EfficiencyRecord.class, c, session);
        for (int i = 0; i < list.size(); i++) {
            hibernateFacade.remove(list.get(i), session);
        }
    }

    public List allCMClasses(Session session) {
        return hibernateFacade.getAll(ControlMeasureClass.class, Order.asc("name"), session);
    }

    public ControlMeasureClass getCMClass(Session session, String name) {
        return (ControlMeasureClass)hibernateFacade.load(ControlMeasureClass.class, Restrictions.eq("name", name), session);
    }

    public List getLightControlMeasures(Session session) {
        return hibernateFacade.getAll(LightControlMeasure.class, Order.asc("name"), session);
    }

    public List getEfficiencyRecords(int controlMeasureId, Session session) {
        Criterion c = Restrictions.eq("controlMeasureId", new Integer(controlMeasureId));
        return hibernateFacade.get(EfficiencyRecord.class, c, session);
    }

    public EfficiencyRecord[] getEfficiencyRecords(int controlMeasureId, int recordLimit, String filter, EmfDbServer DbServer) throws EmfException {
        try {
            RetrieveEfficiencyRecord retrieveEfficiencyRecord = new RetrieveEfficiencyRecord(controlMeasureId, DbServer);
            return retrieveEfficiencyRecord.getEfficiencyRecords(recordLimit, filter);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    public int addEfficiencyRecord(EfficiencyRecord efficiencyRecord, Session session) {
        hibernateFacade.add(efficiencyRecord, session);
        return efficiencyRecord.getId();
    }

    public void removeEfficiencyRecord(int efficiencyRecordId, Session session) {
        hibernateFacade.remove(hibernateFacade.current(efficiencyRecordId, EfficiencyRecord.class, session), session);
    }

    public void updateEfficiencyRecord(EfficiencyRecord efficiencyRecord, Session session) {
        hibernateFacade.saveOrUpdate(efficiencyRecord, session);
    }

    public ControlMeasure[] getSummaryControlMeasures(DbServer DbServer) throws EmfException {
        try {
            RetrieveControlMeasure retrieveControlMeasure = new RetrieveControlMeasure(DbServer);
            return retrieveControlMeasure.getControlMeasures();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    public ControlMeasure[] getSummaryControlMeasures(int majorPollutantId, DbServer DbServer) throws EmfException {
        try {
            RetrieveControlMeasure retrieveControlMeasure = new RetrieveControlMeasure(DbServer);
            return retrieveControlMeasure.getControlMeasures(majorPollutantId);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }
}