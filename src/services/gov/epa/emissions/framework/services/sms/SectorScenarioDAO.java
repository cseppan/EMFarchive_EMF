package gov.epa.emissions.framework.services.sms;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.EmfProperty;
import gov.epa.emissions.framework.services.data.DataServiceImpl;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.services.persistence.LockingScheme;
import gov.epa.emissions.framework.tasks.DebugLevels;


public class SectorScenarioDAO {
    private LockingScheme lockingScheme;

    private HibernateFacade hibernateFacade;

    private HibernateSessionFactory sessionFactory;

    private DbServerFactory dbServerFactory;
    
    private DatasetDAO datasetDao;

    public SectorScenarioDAO() {
        lockingScheme = new LockingScheme();
        hibernateFacade = new HibernateFacade();
        this.datasetDao = new DatasetDAO();
    }

    public SectorScenarioDAO(DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory) {
        this();
        this.dbServerFactory = dbServerFactory;
        this.sessionFactory = sessionFactory;
    }

    public int add(SectorScenario element, Session session) {
        return addObject(element, session);
    }

    public int add(SectorScenarioOutput element, Session session) {
        return addObject(element, session);
    }

    private int addObject(Object obj, Session session) {
        return (Integer)hibernateFacade.add(obj, session);
    }

    public String getSectorScenarioRunStatus(int sectorScenarioId, Session session) {
        return (String)session.createQuery("select cS.runStatus from SectorScenario cS where cS.id = " + sectorScenarioId).uniqueResult();
    }

    public Long getSectorScenarioRunningCount(Session session) {
        return (Long)session.createQuery("select count(*) as total from SectorScenario cS where cS.runStatus = 'Running'").uniqueResult();
    }

    public List<SectorScenario> getSectorScenariosByRunStatus(String runStatus, Session session) {
//        Criterion critRunStatus = Restrictions.eq("runStatus", runStatus);
//        return hibernateFacade.get(SectorScenario.class, critRunStatus, Order.asc("lastModifiedDate"), session);
//
        return session.createQuery("select new SectorScenario(cS.id, cS.name) from SectorScenario cS where cS.runStatus = :runStatus order by cS.lastModifiedDate").setString("runStatus", runStatus).list();
    }

    public void setSectorScenarioRunStatusAndCompletionDate(int sectorScenarioId, String runStatus, Date completionDate, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.createQuery("update SectorScenario set runStatus = :status, lastModifiedDate = :date, completionDate = :completionDate where id = :id")
            .setString("status", runStatus)
            .setTimestamp("date", new Date())
            .setTimestamp("completionDate", completionDate)
            .setInteger("id", sectorScenarioId)
            .executeUpdate();
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    // return SectorScenarios orderby name
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
        return session.createQuery("select new SectorScenario(cS.id, cS.name, " +
                "cS.lastModifiedDate, cS.runStatus, " +
                "cS.region, cS.targetPollutant, " +
                "cS.project, cS.strategyType, " +
                "cS.costYear, cS.inventoryYear, " +
//                "cS.creator, (select sum(sR.totalCost) from SectorScenarioResult sR where sR.sectorScenarioId = cS.id), (select sum(sR.totalReduction) from SectorScenarioResult sR where sR.sectorScenarioId = cS.id)) " +
                "cS.creator, cS.totalCost, cS.totalReduction) " +
                "from SectorScenario cS " +
                "left join cS.targetPollutant " +
                "left join cS.strategyType " +
                "left join cS.region " +
                "left join cS.project " +
                "left join cS.region " +
                "order by cS.name").list();
        //return hibernateFacade.getAll(SectorScenario.class, Order.asc("name"), session);
    }
//    // return SectorScenarios orderby name
//    public List test(Session session) {
//        //check if dataset is a input inventory for some strategy (via the StrategyInputDataset table)
//        List list = session.createQuery("select cS.name from SectorScenario as cS inner join cS.sectorScenarioInputDatasets as iDs inner join iDs.inputDataset as iD with iD.id = 1221").list();
//        //check if dataset is a input inventory for some strategy (via the StrategyResult table, could be here for historical reasons)
//        list = session.createQuery("select cS.name from SectorScenarioResult sR, SectorScenario cS where sR.sectorScenarioId = cS.id and sR.inputDataset.id = 1221").list();
//        //check if dataset is a detailed result dataset for some strategy
//        list = session.createQuery("select cS.name from SectorScenarioResult sR, SectorScenario cS where sR.sectorScenarioId = cS.id and sR.detailedResultDataset.id = 1221").list();
//        //check if dataset is a controlled inventory for some strategy
//        list = session.createQuery("select cS.name from SectorScenarioResult sR, SectorScenario cS where sR.sectorScenarioId = cS.id and sR.controlledInventoryDataset.id = 1221").list();
//        //check if dataset is used as a region/county dataset for specific strategy measures
//        list = session.createQuery("select cS.name from SectorScenario as cS inner join cS.controlMeasures as cM inner join cM.regionDataset as rD with rD.id = 1221").list();
//        //check if dataset is used as a region/county dataset for specific strategy
//        list = session.createQuery("select cS.name from SectorScenario cS where cS.countyDataset.id = 1221").list();
//
//        return list;
//    }

    public List getAllStrategyTypes(Session session) {
        return hibernateFacade.getAll(SectorScenarioOutputType.class, Order.asc("name"), session);
    }

//    // TODO: gettig all the strategies to obtain the lock--- is it a good idea?
//    public SectorScenario obtainLocked(User owner, SectorScenario element, Session session) {
//        return (SectorScenario) lockingScheme.getLocked(owner, current(element, session), session);
//    }
//
    public SectorScenario obtainLocked(User owner, int id, Session session) {
        return (SectorScenario) lockingScheme.getLocked(owner, current(id, SectorScenario.class, session), session);
    }

//    public void releaseLocked(SectorScenario locked, Session session) {
//        SectorScenario current = current(locked, session);
//        String runStatus = current.getRunStatus();
//        if (runStatus == null || !runStatus.equalsIgnoreCase("Running"))
//            lockingScheme.releaseLock(current, session);
//    }

    public void releaseLocked(User user, int id, Session session) {
        SectorScenario current = getById(id, session);
        String runStatus = current.getRunStatus();
        if (runStatus == null || !runStatus.equalsIgnoreCase("Running"))
            lockingScheme.releaseLock(user, current, session);
    }

    public SectorScenario update(SectorScenario locked, Session session) throws EmfException {
        return (SectorScenario) lockingScheme.releaseLockOnUpdate(locked, current(locked, session), session);
    }

    public SectorScenario updateWithLock(SectorScenario locked, Session session) throws EmfException {
        return (SectorScenario) lockingScheme.renewLockOnUpdate(locked, current(locked, session), session);
    }

    private SectorScenario current(SectorScenario strategy, Session session) {
        return current(strategy.getId(), SectorScenario.class, session);
    }

    public boolean canUpdate(SectorScenario sectorScenario, Session session) {
        if (!exists(sectorScenario.getId(), SectorScenario.class, session)) {
            return false;
        }

        SectorScenario current = current(sectorScenario.getId(), SectorScenario.class, session);

        session.clear();// clear to flush current

        if (current.getName().equals(sectorScenario.getName()))
            return true;

        return !nameUsed(sectorScenario.getName(), SectorScenario.class, session);
    }

    public boolean nameUsed(String name, Class clazz, Session session) {
        return hibernateFacade.nameUsed(name, clazz, session);
    }

    private SectorScenario current(int id, Class clazz, Session session) {
        return (SectorScenario) hibernateFacade.current(id, clazz, session);
    }

    public boolean exists(int id, Class clazz, Session session) {
        return hibernateFacade.exists(id, clazz, session);
    }

    public void remove(SectorScenario strategy, Session session) {
        hibernateFacade.remove(strategy, session);
    }

    public void remove(SectorScenarioOutput result, Session session) {
        hibernateFacade.remove(result, session);
    }

    public SectorScenarioOutputType getSectorScenarioOutputType(String name, Session session) {
        Criterion critName = Restrictions.eq("name", name);
        return (SectorScenarioOutputType)hibernateFacade.load(SectorScenarioOutputType.class, critName, session);
    }

    public SectorScenarioOutput getSectorScenarioOutput(int sectorScenarioId, int inputDatasetId, 
            int detailedResultDatasetId, Session session) {
        Criterion critSectorScenarioId = Restrictions.eq("sectorScenarioId", sectorScenarioId);
        Criterion critInputDatasetId = Restrictions.eq("inputDatasetId", inputDatasetId);
        Criterion critDetailedResultDatasetId = Restrictions.eq("detailedResultDataset.id", detailedResultDatasetId);
        return (SectorScenarioOutput)hibernateFacade.load(SectorScenarioOutput.class, new Criterion[] {critSectorScenarioId, critInputDatasetId, critDetailedResultDatasetId}, 
                session);
    }

    public SectorScenarioOutput getSectorScenarioOutput(int id, Session session) {
        Criterion critId = Restrictions.eq("id", id);
        return (SectorScenarioOutput)hibernateFacade.load(SectorScenarioOutput.class, new Criterion[] {critId}, 
                session);
    }

//    private void updateSectorScenarioIds(SectorScenario sectorScenario, Session session) {
//        Criterion c1 = Restrictions.eq("name", sectorScenario.getName());
//        List list = hibernateFacade.get(SectorScenario.class, c1, session);
//        if (!list.isEmpty()) {
//            SectorScenario cs = (SectorScenario) list.get(0);
//            sectorScenario.setId(cs.getId());
//        }
//    }
//
    public void updateSectorScenarioOutput(SectorScenarioOutput result, Session session) {
        hibernateFacade.saveOrUpdate(result, session);
    }

    public String sectorScenarioRunStatus(int id, Session session) {
        SectorScenario sectorScenario = (SectorScenario) hibernateFacade.current(id, SectorScenario.class, session);
        return sectorScenario.getRunStatus();
    }

//    public void removeSectorScenarioResult(SectorScenario sectorScenario, Session session) {
//        Criterion c = Restrictions.eq("sectorScenarioId", new Integer(sectorScenario.getId()));
//        List list = hibernateFacade.get(SectorScenarioResult.class, c, session);
//        for (int i = 0; i < list.size(); i++) {
//            SectorScenarioResult result = (SectorScenarioResult) list.get(i);
//            hibernateFacade.delete(result,session);
//        }
//    }

    public void removeSectorScenarioResults(int sectorScenarioId, Session session) {
        String hqlDelete = "delete SectorScenarioResult sr where sr.sectorScenarioId = :sectorScenarioId";
        session.createQuery( hqlDelete )
             .setInteger("sectorScenarioId", sectorScenarioId)
             .executeUpdate();
        session.flush();
    }

    public void removeSectorScenarioResult(int sectorScenarioId, int resultId, Session session) {
        String hqlDelete = "delete SectorScenarioResult sr where sr.id = :resultId and sr.sectorScenarioId = :sectorScenarioId";
        session.createQuery( hqlDelete )
             .setInteger("resultId", resultId)
             .setInteger("sectorScenarioId", sectorScenarioId)
             .executeUpdate();
        session.flush();
    }

    public SectorScenario getByName(String name, Session session) {
        SectorScenario cs = (SectorScenario) hibernateFacade.load(SectorScenario.class, Restrictions.eq("name", new String(name)), session);
        return cs;
    }

    public SectorScenario getById(int id, Session session) {
        SectorScenario cs = (SectorScenario) hibernateFacade.load(SectorScenario.class, Restrictions.eq("id", new Integer(id)), session);
        return cs;
    }

    public List<SectorScenarioOutput> getSectorScenarioOutputs(int sectorScenarioId, Session session) {
        return session.createCriteria(SectorScenarioOutput.class).add(Restrictions.eq("sectorScenarioId", sectorScenarioId)).addOrder(Order.desc("startTime")).list();
    }
    
    public void removeResultDatasets(EmfDataset[] datasets, User user, Session session, DbServer dbServer) throws EmfException {
        if (datasets != null) {
            try {
                deleteDatasets(datasets, user, session);
                datasetDao.deleteDatasets(datasets, dbServer, session);
            } catch (EmfException e) {
                if (DebugLevels.DEBUG_12)
                    System.out.println(e.getMessage());
                
                throw new EmfException(e.getMessage());
            }
        }
    }
    
    public void deleteDatasets(EmfDataset[] datasets, User user, Session session) throws EmfException {
        EmfDataset[] lockedDatasets = getLockedDatasets(datasets, user, session);
        
        if (lockedDatasets == null)
            return;
        
        try {
            new DataServiceImpl(dbServerFactory, sessionFactory).deleteDatasets(user, datasets);
        } catch (EmfException e) {
            releaseLocked(lockedDatasets, user, session);
            throw new EmfException(e.getMessage());
        }
    }
    
    private EmfDataset[] getLockedDatasets(EmfDataset[] datasets, User user, Session session) {
        List lockedList = new ArrayList();
        
        for (int i = 0; i < datasets.length; i++) {
            EmfDataset locked = obtainLockedDataset(datasets[i], user, session);
            if (locked == null) {
                releaseLocked((EmfDataset[])lockedList.toArray(new EmfDataset[0]), user, session);
                return null;
            }
            
            lockedList.add(locked);
        }
        
        return (EmfDataset[])lockedList.toArray(new EmfDataset[0]);
    }

    private EmfDataset obtainLockedDataset(EmfDataset dataset, User user, Session session) {
        EmfDataset locked = datasetDao.obtainLocked(user, dataset, session);
        return locked;
    }
    
    private void releaseLocked(EmfDataset[] lockedDatasets, User user, Session session) {
        if (lockedDatasets.length == 0)
            return;
        
        for(int i = 0; i < lockedDatasets.length; i++)
            datasetDao.releaseLocked(user, lockedDatasets[i], session);
    }
//    public void removeResultDatasets(Integer[] ids, User user, Session session, DbServer dbServer) throws EmfException {
//        DatasetDAO dsDao = new DatasetDAO();
//        for (Integer id : ids ) {
//            EmfDataset dataset = dsDao.getDataset(session, id);
//
//            if (dataset != null) {
//                try {
//                    dsDao.remove(user, dataset, session);
//                    purgeDeletedDatasets(dataset, session, dbServer);
//                    session.flush();
//                    session.clear();
//                } catch (EmfException e) {
//                    if (DebugLevels.DEBUG_12)
//                        System.out.println(e.getMessage());
//                    
//                    throw new EmfException(e.getMessage());
//                }
//            }
//        }
//    }
    
//    private void purgeDeletedDatasets(EmfDataset dataset, Session session, DbServer dbServer) throws EmfException {
//        try {
//            DatasetDAO dao = new DatasetDAO();
//            dao.deleteDatasets(new EmfDataset[] {dataset}, dbServer, session);
//        } catch (Exception e) {
//            throw new EmfException(e.getMessage());
//        } finally {
//            //
//        }
//    }

    public Integer[] getResultDatasetIds(int sectorScenarioId, Session session) {
        List<SectorScenarioOutput> results = getSectorScenarioOutputs(sectorScenarioId, session);
        List<Integer> datasetLists = new ArrayList<Integer>();
        if(results != null){
            System.out.println(results.size());
            for (int i=0; i<results.size(); i++){
                datasetLists.add( results.get(i).getOutputDataset().getId());
            }
        }
        if (datasetLists.size()>0)
            return datasetLists.toArray(new Integer[0]);
        return null; 
    }

    
    public EmfDataset[] getOutputDatasets(int sectorScenarioId, Session session) {
        List<SectorScenarioOutput> results = getSectorScenarioOutputs(sectorScenarioId, session);
        List<EmfDataset> datasets = new ArrayList<EmfDataset>();
        if(results != null){
            for (int i=0; i<results.size(); i++){
                if (results.get(i).getOutputDataset() != null)
                    datasets.add(results.get(i).getOutputDataset());
            }
        }
        if (datasets.size()>0)
            return datasets.toArray(new EmfDataset[0]);
        return null; 
    }

    public void setSectorScenarioRunStatus(int id, String runStatus, Date completionDate, Session session) {
        // NOTE Auto-generated method stub
        
    }

    public String getDefaultExportDirectory(Session session) {
        EmfProperty tmpDir = new EmfPropertiesDAO().getProperty("ImportExportTempDir", session);
        String dir = "";
        if (tmpDir != null)
            dir = tmpDir.getValue();
        return dir;
    }

    public String getStrategyRunStatus(Session session, int id) {
        return (String)session.createQuery("select cS.runStatus " +
                "from SectorScenario cS where cS.id = " + id).uniqueResult();
    }

}
