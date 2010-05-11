package gov.epa.emissions.framework.services.sms;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.io.DeepCopy;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.SectorScenarioService;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class SectorScenarioServiceImpl implements SectorScenarioService {

    private static Log LOG = LogFactory.getLog(SectorScenarioServiceImpl.class);

    private PooledExecutor threadPool;

    private HibernateSessionFactory sessionFactory;

    protected DbServerFactory dbServerFactory;

    private SectorScenarioDAO dao;

    public SectorScenarioServiceImpl() throws Exception {
        init(HibernateSessionFactory.get(), DbServerFactory.get());
    }

    public SectorScenarioServiceImpl(HibernateSessionFactory sessionFactory, DbServerFactory dbServerFactory)
            throws Exception {
        init(sessionFactory, dbServerFactory);
    }

    private synchronized void init(HibernateSessionFactory sessionFactory, DbServerFactory dbServerFactory) {
        this.sessionFactory = sessionFactory;
        this.dbServerFactory = dbServerFactory;
        dao = new SectorScenarioDAO();
        threadPool = createThreadPool();

    }

    protected synchronized void finalize() throws Throwable {
        threadPool.shutdownAfterProcessingCurrentlyQueuedTasks();
        threadPool.awaitTerminationAfterShutdown();
        super.finalize();
    }

    private synchronized PooledExecutor createThreadPool() {
        PooledExecutor threadPool = new PooledExecutor(20);
        threadPool.setMinimumPoolSize(1);
        threadPool.setKeepAliveTime(1000 * 60 * 3);// terminate after 3 (unused) minutes

        return threadPool;
    }

    public synchronized SectorScenario[] getSectorScenarios() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List cs = dao.all(session);
            return (SectorScenario[]) cs.toArray(new SectorScenario[0]);
        } catch (HibernateException e) {
            LOG.error("Could not retrieve all sector scenarios." +e.getMessage());
            throw new EmfException("Could not retrieve all sector scenarios.");
        } finally {
            session.close();
        }
    }

    public synchronized int addSectorScenario(SectorScenario element) throws EmfException {
        Session session = sessionFactory.getSession();
        int csId;
        try {
            csId = dao.add(element, session);
        } catch (RuntimeException e) {
            LOG.error("Could not add Control Strategy: " + element, e);
            throw new EmfException("Could not add Control Strategy: " + element);
        } finally {
            session.close();
        }
        return csId;
    }

    public synchronized void setSectorScenarioRunStatusAndCompletionDate(int id, String runStatus, Date completionDate)
            throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.setSectorScenarioRunStatusAndCompletionDate(id, runStatus, completionDate, session);
        } catch (RuntimeException e) {
            LOG.error("Could not set Control Strategy run status: " + id, e);
            throw new EmfException("Could not add Control Strategy run status: " + id);
        } finally {
            session.close();
        }
    }

    public synchronized SectorScenario obtainLocked(User owner, int id) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            SectorScenario locked = dao.obtainLocked(owner, id, session);

            return locked;
        } catch (RuntimeException e) {
            LOG
                    .error("Could not obtain lock for Control Strategy: id = " + id + " by owner: "
                            + owner.getUsername(), e);
            throw new EmfException("Could not obtain lock for Control Strategy: id = " + id + " by owner: "
                    + owner.getUsername());
        } finally {
            session.close();
        }
    }

    // FIXME
    // public void releaseLocked(SectorScenario locked) throws EmfException {
    // Session session = sessionFactory.getSession();
    // try {
    // dao.releaseLocked(locked, session);
    // } catch (RuntimeException e) {
    // LOG.error(
    // "Could not release lock for Control Strategy : " + locked + " by owner: " + locked.getLockOwner(),
    // e);
    // throw new EmfException("Could not release lock for Control Strategy: " + locked + " by owner: "
    // + locked.getLockOwner());
    // } finally {
    // session.close();
    // }
    // }

    public synchronized void releaseLocked(User user, int id) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.releaseLocked(user, id, session);
        } catch (RuntimeException e) {
            LOG.error("Could not release lock for Control Strategy id: " + id, e);
            throw new EmfException("Could not release lock for Control Strategy id: " + id);
        } finally {
            session.close();
        }
    }

    public synchronized SectorScenario updateSectorScenario(SectorScenario element) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            if (!dao.canUpdate(element, session))
                throw new EmfException("The Control Strategy name is already in use");

            SectorScenario released = dao.update(element, session);

            return released;
        } catch (RuntimeException e) {
            LOG.error("Could not update Control Strategy: " + element, e);
            throw new EmfException("Could not update SectorScenario: " + element);
        } finally {
            session.close();
        }
    }

    public synchronized SectorScenario updateSectorScenarioWithLock(SectorScenario element) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            if (!dao.canUpdate(element, session))
                throw new EmfException("Control Strategy name already in use");

            SectorScenario csWithLock = dao.updateWithLock(element, session);

            return csWithLock;
            // return dao.getById(csWithLock.getId(), session);
        } catch (RuntimeException e) {
            LOG.error("Could not update Control Strategy: " + element, e);
            throw new EmfException("Could not update SectorScenario: " + element);
        } finally {
            session.close();
        }
    }

    // public void removeSectorScenarios(SectorScenario[] elements, User user) throws EmfException {
    // try {
    // for (int i = 0; i < elements.length; i++) {
    // if (!user.equals(elements[i].getCreator()))
    // throw new EmfException("Only the creator of " + elements[i].getName()
    // + " can remove it from the database.");
    // remove(elements[i]);
    // }
    //
    // } catch (RuntimeException e) {
    // LOG.error("Could not update Control Strategy: " + elements, e);
    // throw new EmfException("Could not update SectorScenario: " + elements);
    // }
    // }

    public synchronized void removeSectorScenarios(int[] ids, User user) throws EmfException {
        Session session = sessionFactory.getSession();
        String exception = "";
        try {
            for (int i = 0; i < ids.length; i++) {
                SectorScenario cs = dao.getById(ids[i], session);
                session.clear();

                // check if admin user, then allow it to be removed.
                if (user.equals(cs.getCreator()) || user.isAdmin()) {
                    if (cs.isLocked())
                        exception += "The control strategy, " + cs.getName()
                                + ", is in edit mode and can not be removed. ";
                    else
                        remove(cs);
                } else {
                    exception += "You do not have permission to remove the strategy: " + cs.getName() + ". ";
                }
            }

            if (exception.length() > 0)
                throw new EmfException(exception);
        } catch (RuntimeException e) {
            LOG.error("Could not remove Control Strategy", e);
            throw new EmfException("Could not remove SectorScenario");
        } finally {
            session.close();
        }
    }

    private synchronized void remove(SectorScenario element) throws EmfException {
        Session session = sessionFactory.getSession();
        try {

            if (!dao.canUpdate(element, session))
                throw new EmfException("Control Strategy name already in use");

            SectorScenarioOutput[] controlStrategyResults = getSectorScenarioOutputs(element.getId());
            for (int i = 0; i < controlStrategyResults.length; i++) {
                dao.remove(controlStrategyResults[i], session);
            }

            dao.remove(element, session);
        } catch (RuntimeException e) {
            LOG.error("Could not remove control strategy: " + element, e);
            throw new EmfException("Could not remove control strategy: " + element.getName());
        } finally {
            session.close();
        }
    }

    public synchronized void removeResultDatasets(Integer[] ids, User user) throws EmfException {
        Session session = sessionFactory.getSession();
        DatasetDAO dsDao = new DatasetDAO();
        try {
            for (Integer id : ids) {
                EmfDataset dataset = dsDao.getDataset(session, id);

                if (dataset != null) {
                    try {
                        dsDao.remove(user, dataset, session);
                    } catch (EmfException e) {
                        if (DebugLevels.DEBUG_12)
                            System.out.println(e.getMessage());

                        throw new EmfException(e.getMessage());
                    }
                }
            }
        } finally {
            session.close();
        }
    }

    public synchronized void runStrategy(User user, int sectorScenarioId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            // first see if the strategy has been canceled, is so don't run it...
            String runStatus = dao.getSectorScenarioRunStatus(sectorScenarioId, session);
            if (runStatus.equals("Cancelled"))
                return;

            SectorScenario strategy = getById(sectorScenarioId);
//            validateSectors(strategy);
            //get rid of for now, since we don't auto export anything
            //make sure a valid server-side export path was specified
            //validateExportPath(strategy.getExportDirectory());
            
            //make the runner of the strategy is the owner of the strategy...
            //NEED TO TALK TO ALISON ABOUT ISSUES, LOCEKD owner might not be the creator of resulting datsets,
            //hence a exception when trying to purge/delete the resulting datasets
            //if (control);
            

            // queue up the strategy to be run, by setting runStatus to Waiting
            dao.setSectorScenarioRunStatusAndCompletionDate(sectorScenarioId, "Waiting", null, session);

//            validatePath(strategy.getExportDirectory());
            RunSectorScenario runStrategy = new RunSectorScenario(sessionFactory, dbServerFactory,
                    threadPool);
            runStrategy.run(user, strategy, this);
        } catch (EmfException e) {
            // queue up the strategy to be run, by setting runStatus to Waiting
            dao.setSectorScenarioRunStatusAndCompletionDate(sectorScenarioId, "Failed", null, session);

            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }
    }

//    private void validateSectors(SectorScenario strategy) throws EmfException {
//        SectorScenarioInventory[] inputDatasets = strategy.getInventories();
//        if (inputDatasets == null || inputDatasets.length == 0)
//            throw new EmfException("Input Dataset does not exist. ");
//        for (SectorScenarioInventory dataset : inputDatasets) {
//            Sector[] sectors = dataset.getInputDataset().getSectors();
//            if (sectors == null || sectors.length == 0)
//                throw new EmfException("Inventory, " + dataset.getInputDataset().getName() + ", is missing a sector.  Edit dataset to add sector.");
//        }
//    }

    public List<SectorScenario> getSectorScenariosByRunStatus(String runStatus) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return dao.getSectorScenariosByRunStatus(runStatus, session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not get Control Strategies by run status: " + runStatus);
        } finally {
            session.close();
        }
    }

    public Long getSectorScenarioRunningCount() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return dao.getSectorScenarioRunningCount(session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not get Control Strategies running count");
        } finally {
            session.close();
        }
    }

//    private File validatePath(String folderPath) throws EmfException {
//        File file = new File(folderPath);
//
//        if (!file.exists() || !file.isDirectory()) {
//            LOG.error("Folder " + folderPath + " does not exist");
//            throw new EmfException("Export folder does not exist: " + folderPath);
//        }
//        return file;
//    }

    public synchronized void stopRunStrategy(int sectorScenarioId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            // look at the current status, if waiting or running, then update to Cancelled.
            String status = dao.getSectorScenarioRunStatus(sectorScenarioId, session);
            if (status.toLowerCase().startsWith("waiting") || status.toLowerCase().startsWith("running"))
                dao.setSectorScenarioRunStatusAndCompletionDate(sectorScenarioId, "Cancelled", null, session);
        } catch (RuntimeException e) {
            LOG.error("Could not set Control Strategy run status: " + sectorScenarioId, e);
            throw new EmfException("Could not add Control Strategy run status: " + sectorScenarioId);
        } finally {
            session.close();
        }
    }

    public synchronized String sectorScenarioRunStatus(int id) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return dao.sectorScenarioRunStatus(id, session);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve SectorScenario Status", e);
            throw new EmfException("Could not retrieve SectorScenario Status");
        } finally {
            session.close();
        }
    }

    // returns control strategy Id for the given name
    public synchronized int isDuplicateName(String name) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            SectorScenario cs = dao.getByName(name, session);
            return cs == null ? 0 : cs.getId();
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve if SectorScenario name is already used", e);
            throw new EmfException("Could not retrieve if SectorScenario name is already used");
        } finally {
            session.close();
        }
    }

    public synchronized int copySectorScenario(int id, User creator) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            // get cs to copy
            SectorScenario cs = dao.getById(id, session);

            session.clear();// clear to flush current

            String name = "Copy of " + cs.getName();
            // make sure this won't cause duplicate issues...
            if (isDuplicate(name))
                throw new EmfException("A control strategy named '" + name + "' already exists.");

            // do a deep copy
            SectorScenario copied = (SectorScenario) DeepCopy.copy(cs);
            // change to applicable values
            copied.setName(name);
            copied.setCreator(creator);
            copied.setLastModifiedDate(new Date());
            copied.setRunStatus("Not started");
            copied.setCopiedFrom(cs.getName());
            if (copied.isLocked()) {
                copied.setLockDate(null);
                copied.setLockOwner(null);
            }

            dao.add(copied, session);
            int csId = copied.getId();
            return csId;
        } catch (EmfException e) {
            LOG.error(e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            LOG.error("Could not copy control strategy", e);
            throw new EmfException("Could not copy control strategy");
        } catch (Exception e) {
            LOG.error("Could not copy control strategy", e);
            throw new EmfException("Could not copy control strategy");
        } finally {
            session.close();
        }
    }

    private synchronized boolean isDuplicate(String name) throws EmfException {
        return (isDuplicateName(name) != 0);
    }

    public synchronized SectorScenario getById(int id) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return dao.getById(id, session);
        } catch (RuntimeException e) {
            LOG.error("Could not get control strategy", e);
            throw new EmfException("Could not get control strategy");
        } finally {
            session.close();
        }
    }

    public synchronized SectorScenarioOutput[] getSectorScenarioOutputs(int sectorScenarioId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List all = dao.getSectorScenarioOutputs(sectorScenarioId, session);
            return (SectorScenarioOutput[]) all.toArray(new SectorScenarioOutput[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve control strategy results.", e);
            throw new EmfException("Could not retrieve control strategy results.");
        } finally {
            session.close();
        }
    }

    public synchronized String getDefaultExportDirectory() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            String dir = dao.getDefaultExportDirectory(session);
            return dir;
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve default export directory.", e);
            throw new EmfException("Could not retrieve default export directory.");
        } finally {
            session.close();
        }
    }

    public synchronized String getStrategyRunStatus(int id) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return dao.getStrategyRunStatus(session, id);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve strategy run status.", e);
            throw new EmfException("Could not retrieve strategy run status.");
        } finally {
            session.close();
        }
    }

    public synchronized String[] getDistinctSectorListFromDataset(int datasetId, int versionNumber) throws EmfException {
        Session session = sessionFactory.getSession();
        DbServer dbServer = dbServerFactory.getDbServer();
        try {
            return dao.getDistinctSectorListFromDataset(session, dbServer, datasetId, versionNumber);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve distinct sector list from a dataset.", e);
            throw new EmfException("Could not retrieve distinct sector list from a dataset.");
        } finally {
            try {
                session.close();
            } catch (Exception e) {
                throw new EmfException("Could not close hibernate session - " + e.getMessage());
            }
            try {
                dbServer.disconnect();
            } catch (Exception e) {
                throw new EmfException("Could not disconnect DbServer - " + e.getMessage());
            }
        }
    }
}
