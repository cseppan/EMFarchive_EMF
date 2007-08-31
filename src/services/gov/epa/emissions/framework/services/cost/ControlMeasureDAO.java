package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.LockingScheme;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class ControlMeasureDAO {
//    private static Log LOG = LogFactory.getLog(ControlMeasureDAO.class);

    private LockingScheme lockingScheme;

    private HibernateFacade hibernateFacade;
    
//    private DbServer dbServer;
    
    public ControlMeasureDAO() {
        lockingScheme = new LockingScheme();
        hibernateFacade = new HibernateFacade();
    }

//    public ControlMeasureDAO(DbServer dbServer) {
//        lockingScheme = new LockingScheme();
//        hibernateFacade = new HibernateFacade();
//        this.dbServer = dbServer;
//    }

    public boolean exists(int id, Class clazz, Session session) {
        return hibernateFacade.exists(id, clazz, session);
    }

    public boolean exists(Class clazz, Criterion[] criterions, Session session) {
        return hibernateFacade.exists(clazz, criterions, session);
    }

    private boolean exists(Class clazz, Criterion[] criterions, StatelessSession session) {
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
        session.flush();
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

    public int copy(int controlMeasureId, User creator, Session session, DbServer dbServer) throws EmfException {
        ControlMeasure cm = current(controlMeasureId, session);
        session.clear();//must do this
        
        //set the name and give a random abbrev...
        cm.setName("Copy of " + cm.getName());
        cm.setAbbreviation(Math.round(Math.random() * 1000000000) % 1000000000 + "");
        
        //make sure the name and abbrev are unique
        Criterion name = Restrictions.eq("name", cm.getName());
        Criterion abbrev = Restrictions.eq("abbreviation", cm.getAbbreviation());
        boolean abbrExist = abbrExist(new Criterion[] { abbrev }, session);
        boolean nameExist = nameExist(new Criterion[] { name }, session);

        if (abbrExist || nameExist)
            throw new EmfException("A control measure with the same name or abbreviation already exists.");

        //default appropriate values
        cm.setCreator(creator);
        cm.setLastModifiedTime(new Date());
        cm.setLastModifiedBy(creator.getName());

        //create new measure, get its id
        int cmId = (Integer)hibernateFacade.add(cm, session);

        //copy measure SCCs
        Scc[] sccs = getSccs(controlMeasureId, session);
        session.clear();//must do this
        updateSccsControlMeasureIds(sccs, cmId);
        hibernateFacade.add(sccs, session);
        
        //copy measure Efficiecny Records
        EfficiencyRecord[] records = (EfficiencyRecord[]) getEfficiencyRecords(controlMeasureId, session).toArray(new EfficiencyRecord[0]);
        session.clear();//must do this
        updateEfficiencyRecordControlMeasureIds(records, cmId);
        hibernateFacade.add(records, session);
        
        //populate aggregate Efficiecny Records
        updateAggregateEfficiencyRecords(cmId, dbServer);

        return cmId;
    }

    private void removeSccs(int controlMeasureId, Session session) {
        String hqlDelete = "delete Scc scc where scc.controlMeasureId = :controlMeasureId";
        session.createQuery( hqlDelete )
             .setInteger("controlMeasureId", controlMeasureId)
             .executeUpdate();
        session.flush();
//        Scc[] sccs = getSccs(controlMeasureId, session);
//        for (int i = 0; i < sccs.length; i++) {
//            hibernateFacade.remove(sccs[i], session);
//        }
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

    private void updateEfficiencyRecordControlMeasureIds(EfficiencyRecord[] records, int controlMeasureId) {
        for (int i = 0; i < records.length; i++) {
            records[i].setControlMeasureId(controlMeasureId);
        }
    }

    public Scc[] getSccsWithDescriptions(int controlMeasureId, DbServer dbServer) throws EmfException {
        try {
            RetrieveSCC retrieveSCC = new RetrieveSCC(controlMeasureId, dbServer);
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

    public String[] getCMAbbrevAndSccs(int measureId, DbServer dbServer) throws EmfException {
        try {
            RetrieveSCC retrieveSCC = new RetrieveSCC(measureId, dbServer);
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
            throw new EmfException("This control measure abbreviation is already in use: "
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

    public int addFromImporter(ControlMeasure measure, Scc[] sccs, User user, Session session, DbServer dbServer) throws EmfException {
        int cmId = 0;
        Criterion name = Restrictions.eq("name", measure.getName());
        Criterion abbrev = Restrictions.eq("abbreviation", measure.getAbbreviation());
        boolean abbrExist = abbrExist(new Criterion[] { abbrev }, session);
        boolean nameExist = nameExist(new Criterion[] { name }, session);

        if (nameExist) {// overwrite if name exist regard less of abbrev
            cmId = controlMeasureId(measure, session);
            measure.setId(cmId);
            ControlMeasure obtainLocked = obtainLocked(user, cmId, session);
            if (obtainLocked == null)
                throw new EmfException("Could not obtain the lock to update: " + measure.getName());
            measure.setLockDate(obtainLocked.getLockDate());
            measure.setLockOwner(obtainLocked.getLockOwner());
//            LOG.error("remove Sccs");
            removeSccs(cmId, session);
//            LOG.error("remove EfficiencyRecords");
            //removeEfficiencyRecords(cmId, session);
            removeEfficiencyRecords(cmId, dbServer);
//            LOG.error("remove AggregateEfficiencyRecords");
            AggregateEfficiencyRecordDAO aerDAO = new AggregateEfficiencyRecordDAO();
            aerDAO.removeAggregateEfficiencyRecords(cmId, dbServer);
//            LOG.error("update measure and sccs");
            update(measure, sccs, session);
        } else if (abbrExist) {
            throw new EmfException("This control measure abbreviation is already in use: " + measure.getAbbreviation());
        } else {
            cmId = add(measure, sccs, session);
        }
        return cmId;
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

    public void removeMeasureEquationType(int controlMeasureEquationTypeId, Session session) {
        String hqlDelete = "delete ControlMeasureEquationType et where et.id = :controlMeasureEquationTypeId";
        session.createQuery( hqlDelete )
             .setInteger("controlMeasureEquationTypeId", controlMeasureEquationTypeId)
             .executeUpdate();
        session.flush();
//        Criterion c = Restrictions.eq("controlMeasureId", new Integer(controlMeasureId));
//        List list = hibernateFacade.get(EfficiencyRecord.class, c, session);
//        for (int i = 0; i < list.size(); i++) {
//            hibernateFacade.remove(list.get(i), session);
//        }
    }

    public void removeEfficiencyRecords(int controlMeasureId, Session session) {
        String hqlDelete = "delete EfficiencyRecord er where er.controlMeasureId = :controlMeasureId";
        session.createQuery( hqlDelete )
             .setInteger("controlMeasureId", controlMeasureId)
             .executeUpdate();
        session.flush();
//        Criterion c = Restrictions.eq("controlMeasureId", new Integer(controlMeasureId));
//        List list = hibernateFacade.get(EfficiencyRecord.class, c, session);
//        for (int i = 0; i < list.size(); i++) {
//            hibernateFacade.remove(list.get(i), session);
//        }
    }

    public void removeEfficiencyRecords(int controlMeasureId, DbServer dbServer) throws EmfException {
        try {
            dbServer.getEmfDatasource().query().execute("delete from emf.control_measure_efficiencyrecords " +
                    "where control_measures_id = " + controlMeasureId + ";");
            } catch (SQLException e) {
                throw new EmfException(e.getMessage());
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

    public EfficiencyRecord[] getEfficiencyRecords(int controlMeasureId, int recordLimit, String filter, DbServer dbServer) throws EmfException {
        try {
            RetrieveEfficiencyRecord retrieveEfficiencyRecord = new RetrieveEfficiencyRecord(controlMeasureId, dbServer);
            return retrieveEfficiencyRecord.getEfficiencyRecords(recordLimit, filter);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    public int addEfficiencyRecord(EfficiencyRecord efficiencyRecord, Session session, DbServer dbServer) throws EmfException {
        checkForDuplicateEfficiencyRecord(efficiencyRecord, session);
        hibernateFacade.add(efficiencyRecord, session);
        updateAggregateEfficiencyRecords(efficiencyRecord.getControlMeasureId(), dbServer);
        return efficiencyRecord.getId();
    }

    public int addEfficiencyRecord(EfficiencyRecord efficiencyRecord, StatelessSession session) throws EmfException {
        checkForDuplicateEfficiencyRecord(efficiencyRecord, session);
        session.insert(efficiencyRecord);
//        hibernateFacade.add(efficiencyRecord, session);
        return efficiencyRecord.getId();
    }

    public void checkForDuplicateEfficiencyRecord(EfficiencyRecord record, Session session) throws EmfException {
        Criterion id = Restrictions.ne("id", new Integer(record.getId()));
        Criterion measureId = Restrictions.eq("controlMeasureId", new Integer(record.getControlMeasureId()));
        Criterion locale = Restrictions.eq("locale", record.getLocale());
        Criterion pollutant = Restrictions.eq("pollutant", record.getPollutant());
        Criterion existingMeasureAbbr = record.getExistingMeasureAbbr() == null ? Restrictions.isNull("existingMeasureAbbr") : Restrictions.eq("existingMeasureAbbr", record.getExistingMeasureAbbr());
        Criterion effectiveDate = record.getEffectiveDate() == null ? Restrictions.isNull("effectiveDate") : Restrictions.eq("effectiveDate", record.getEffectiveDate());

        Criterion minEmis = record.getMinEmis() == null ? Restrictions.isNull("minEmis") : Restrictions.eq("minEmis", record.getMinEmis());
        Criterion maxEmis = record.getMaxEmis() == null ? Restrictions.isNull("maxEmis") : Restrictions.eq("maxEmis", record.getMaxEmis());

        if (exists(EfficiencyRecord.class, new Criterion[] {id, measureId, locale, pollutant, existingMeasureAbbr, effectiveDate, minEmis, maxEmis}, session)) {
            throw new EmfException("Duplicate Record: The combination of 'Pollutant', 'Locale', 'Effective Date', 'Existing Measure', 'Minimum Emission' and 'Maximum Emission' should be unique - Locale = " + record.getLocale()
                + " Pollutant = " + record.getPollutant().getName()
                + " ExistingMeasureAbbr = " + record.getExistingMeasureAbbr()
                + " EffectiveDate = " + (record.getEffectiveDate() == null ? "" : record.getEffectiveDate())
                + " MinEmis = " + (record.getMinEmis() == null ? "" : record.getMinEmis())
                + " MaxEmis = " + (record.getMaxEmis() == null ? "" : record.getMaxEmis()));
        }
    }

    public void checkForDuplicateEfficiencyRecord(EfficiencyRecord record, StatelessSession session) throws EmfException {
        Criterion id = Restrictions.ne("id", new Integer(record.getId()));
        Criterion measureId = Restrictions.eq("controlMeasureId", new Integer(record.getControlMeasureId()));
        Criterion locale = Restrictions.eq("locale", record.getLocale());
        Criterion pollutant = Restrictions.eq("pollutant", record.getPollutant());
        Criterion existingMeasureAbbr = record.getExistingMeasureAbbr() == null ? Restrictions.isNull("existingMeasureAbbr") : Restrictions.eq("existingMeasureAbbr", record.getExistingMeasureAbbr());
        Criterion effectiveDate = record.getEffectiveDate() == null ? Restrictions.isNull("effectiveDate") : Restrictions.eq("effectiveDate", record.getEffectiveDate());

        if (exists(EfficiencyRecord.class, new Criterion[] {id, measureId, locale, pollutant, existingMeasureAbbr, effectiveDate}, session)) {
            throw new EmfException("Duplicate Record: The combination of 'Pollutant', 'Locale', 'Effective Date' and 'Existing Measure' should be unique - Locale = " + record.getLocale()
                + " Pollutant = " + record.getPollutant().getName()
                + " ExistingMeasureAbbr = " + record.getExistingMeasureAbbr()
                + " EffectiveDate = " + (record.getEffectiveDate() == null ? "" : record.getEffectiveDate()));
        }
    }

    public void removeEfficiencyRecord(int efficiencyRecordId, Session session, DbServer dbServer) throws EmfException {
        EfficiencyRecord er = (EfficiencyRecord)hibernateFacade.current(efficiencyRecordId, EfficiencyRecord.class, session);
        int cmId = er.getControlMeasureId();
        hibernateFacade.remove(hibernateFacade.current(efficiencyRecordId, EfficiencyRecord.class, session), session);
        updateAggregateEfficiencyRecords(cmId, dbServer);
    }

    public void updateEfficiencyRecord(EfficiencyRecord efficiencyRecord, Session session, DbServer dbServer) throws EmfException {
        checkForDuplicateEfficiencyRecord(efficiencyRecord, session);
        hibernateFacade.saveOrUpdate(efficiencyRecord, session);
        updateAggregateEfficiencyRecords(efficiencyRecord.getControlMeasureId(), dbServer);
    }

    public ControlMeasure[] getSummaryControlMeasures(DbServer dbServer) throws EmfException {
        try {
            RetrieveControlMeasure retrieveControlMeasure = new RetrieveControlMeasure(dbServer);
            return retrieveControlMeasure.getControlMeasures();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    public ControlMeasure[] getSummaryControlMeasures(int majorPollutantId, DbServer dbServer) throws EmfException {
        try {
            RetrieveControlMeasure retrieveControlMeasure = new RetrieveControlMeasure(dbServer);
            return retrieveControlMeasure.getControlMeasures(majorPollutantId);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    private void updateAggregateEfficiencyRecords(int controlMeasureId, DbServer dbServer) throws EmfException {
        try {
            AggregateEfficiencyRecordDAO aerDAO = new AggregateEfficiencyRecordDAO();
            aerDAO.updateAggregateEfficiencyRecords(controlMeasureId, dbServer);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    public void updateAggregateEfficiencyRecords(ControlMeasure[] measures, DbServer dbServer) throws EmfException {
        try {
            AggregateEfficiencyRecordDAO aerDAO = new AggregateEfficiencyRecordDAO();
            for (int i = 0; i < measures.length; i++) {
                aerDAO.updateAggregateEfficiencyRecords(measures[i].getId(), dbServer);
            }
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    public List getEquationTypes(Session session) {
        Query query = session.createQuery("from EquationType as e");
        query.setCacheable(true);
        return query.list();//hibernateFacade.getAll(EquationType.class, Order.asc("name"), session);
    }
}