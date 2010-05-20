package gov.epa.emissions.framework.services.fast;

import gov.epa.emissions.commons.io.DeepCopy;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.GCEnforcerTask;
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

public class FastServiceImpl implements FastService {

    private static Log LOG = LogFactory.getLog(FastServiceImpl.class);

    private PooledExecutor threadPool;

    private HibernateSessionFactory sessionFactory;

    protected DbServerFactory dbServerFactory;

    private FastDAO dao;

    public FastServiceImpl() throws Exception {
        init(HibernateSessionFactory.get(), DbServerFactory.get());
    }

    public FastServiceImpl(HibernateSessionFactory sessionFactory, DbServerFactory dbServerFactory) throws Exception {
        init(sessionFactory, dbServerFactory);
    }

    private synchronized void init(HibernateSessionFactory sessionFactory, DbServerFactory dbServerFactory) {
        this.sessionFactory = sessionFactory;
        this.dbServerFactory = dbServerFactory;
        dao = new FastDAO();
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

    public synchronized FastRun[] getFastRuns() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List cs = dao.getFastRuns(session);
            return (FastRun[]) cs.toArray(new FastRun[0]);
        } catch (HibernateException e) {
            e.printStackTrace();
            LOG.error("Could not retrieve all FastRuns.");
            throw new EmfException("Could not retrieve all FastRuns.");
        } finally {
            session.close();
        }
    }

    public synchronized int addFastRun(FastRun fastRun) throws EmfException {
        Session session = sessionFactory.getSession();
        int csId;
        try {
            csId = dao.add(fastRun, session);
        } catch (RuntimeException e) {
            e.printStackTrace();
            LOG.error("Could not add Control Strategy: " + fastRun, e);
            throw new EmfException("Could not add Control Strategy: " + fastRun);
        } finally {
            session.close();
        }
        return csId;
    }

    public synchronized void setFastRunRunStatusAndCompletionDate(int id, String runStatus, Date completionDate)
            throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.setFastRunRunStatusAndCompletionDate(id, runStatus, completionDate, session);
        } catch (RuntimeException e) {
            LOG.error("Could not set Control Strategy run status: " + id, e);
            throw new EmfException("Could not add Control Strategy run status: " + id);
        } finally {
            session.close();
        }
    }

    public synchronized FastRun obtainLockedFastRun(User owner, int id) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            FastRun locked = dao.obtainLockedFastRun(owner, id, session);

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
    // public void releaseLocked(FastRun locked) throws EmfException {
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

    public synchronized void releaseLockedFastRun(User user, int id) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.releaseLockedFastRun(user, id, session);
        } catch (RuntimeException e) {
            LOG.error("Could not release lock for Control Strategy id: " + id, e);
            throw new EmfException("Could not release lock for Control Strategy id: " + id);
        } finally {
            session.close();
        }
    }

    public synchronized FastRun updateFastRun(FastRun fastRun) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            if (!dao.canUpdateFastRun(fastRun, session))
                throw new EmfException("The Control Strategy name is already in use");

            FastRun released = dao.updateFastRun(fastRun, session);

            return released;
        } catch (RuntimeException e) {
            LOG.error("Could not update Control Strategy: " + fastRun, e);
            throw new EmfException("Could not update FastRun: " + fastRun);
        } finally {
            session.close();
        }
    }

    public synchronized FastRun updateFastRunWithLock(FastRun fastRun) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            if (!dao.canUpdateFastRun(fastRun, session))
                throw new EmfException("Control Strategy name already in use");

            FastRun csWithLock = dao.updateFastRunWithLock(fastRun, session);

            return csWithLock;
            // return dao.getById(csWithLock.getId(), session);
        } catch (RuntimeException e) {
            LOG.error("Could not update Control Strategy: " + fastRun, e);
            throw new EmfException("Could not update FastRun: " + fastRun);
        } finally {
            session.close();
        }
    }

    // public void removeFastRuns(FastRun[] elements, User user) throws EmfException {
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
    // throw new EmfException("Could not update FastRun: " + elements);
    // }
    // }

    public synchronized void removeFastRuns(int[] ids, User user) throws EmfException {
        Session session = sessionFactory.getSession();
        String exception = "";
        try {
            for (int i = 0; i < ids.length; i++) {
                FastRun cs = dao.getFastRun(ids[i], session);
                session.clear();

                // check if admin user, then allow it to be removed.
                if (user.equals(cs.getCreator()) || user.isAdmin()) {
                    if (cs.isLocked())
                        exception += "The control strategy, " + cs.getName()
                                + ", is in edit mode and can not be removed. ";
                    else
                        removeFastRun(cs);
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

    private synchronized void removeFastRun(FastRun fastRun) throws EmfException {
        Session session = sessionFactory.getSession();
        try {

            if (!dao.canUpdateFastRun(fastRun, session))
                throw new EmfException("Control Strategy name already in use");

            FastRunOutput[] controlStrategyResults = getFastRunOutputs(fastRun.getId());
            for (int i = 0; i < controlStrategyResults.length; i++) {
                dao.remove(controlStrategyResults[i], session);
            }

            dao.remove(fastRun, session);
        } catch (RuntimeException e) {
            LOG.error("Could not remove control strategy: " + fastRun, e);
            throw new EmfException("Could not remove control strategy: " + fastRun.getName());
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

    public synchronized void runFastRun(User user, int fastRunId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            // first see if the strategy has been canceled, is so don't run it...
            String runStatus = dao.getFastRunRunStatus(fastRunId, session);
            if (runStatus.equals("Cancelled"))
                return;

            FastRun strategy = getFastRun(fastRunId);
            // validateSectors(strategy);
            // get rid of for now, since we don't auto export anything
            // make sure a valid server-side export path was specified
            // validateExportPath(strategy.getExportDirectory());

            // make the runner of the strategy is the owner of the strategy...
            // NEED TO TALK TO ALISON ABOUT ISSUES, LOCEKD owner might not be the creator of resulting datsets,
            // hence a exception when trying to purge/delete the resulting datasets
            // if (control);

            // queue up the strategy to be run, by setting runStatus to Waiting
            dao.setFastRunRunStatusAndCompletionDate(fastRunId, "Waiting", null, session);

            // validatePath(strategy.getExportDirectory());
            RunFastRun runStrategy = new RunFastRun(sessionFactory, dbServerFactory, threadPool);
            runStrategy.run(user, strategy, this);
        } catch (EmfException e) {
            // queue up the strategy to be run, by setting runStatus to Waiting
            dao.setFastRunRunStatusAndCompletionDate(fastRunId, "Failed", null, session);

            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }
    }

    // private void validateSectors(FastRun strategy) throws EmfException {
    // FastRunInventory[] inputDatasets = strategy.getInventories();
    // if (inputDatasets == null || inputDatasets.length == 0)
    // throw new EmfException("Input Dataset does not exist. ");
    // for (FastRunInventory dataset : inputDatasets) {
    // Sector[] sectors = dataset.getInputDataset().getSectors();
    // if (sectors == null || sectors.length == 0)
    // throw new EmfException("Inventory, " + dataset.getInputDataset().getName() +
    // ", is missing a sector.  Edit dataset to add sector.");
    // }
    // }

    public List<FastRun> getFastRunsByRunStatus(String runStatus) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return dao.getFastRunsByRunStatus(runStatus, session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not get Control Strategies by run status: " + runStatus);
        } finally {
            session.close();
        }
    }

    public Long getFastRunRunningCount() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return dao.getFastRunRunningCount(session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not get Control Strategies running count");
        } finally {
            session.close();
        }
    }

    // private File validatePath(String folderPath) throws EmfException {
    // File file = new File(folderPath);
    //
    // if (!file.exists() || !file.isDirectory()) {
    // LOG.error("Folder " + folderPath + " does not exist");
    // throw new EmfException("Export folder does not exist: " + folderPath);
    // }
    // return file;
    // }

    public synchronized void stopFastRun(int fastRunId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            // look at the current status, if waiting or running, then update to Cancelled.
            String status = dao.getFastRunRunStatus(fastRunId, session);
            if (status.toLowerCase().startsWith("waiting") || status.toLowerCase().startsWith("running"))
                dao.setFastRunRunStatusAndCompletionDate(fastRunId, "Cancelled", null, session);
        } catch (RuntimeException e) {
            LOG.error("Could not set Control Strategy run status: " + fastRunId, e);
            throw new EmfException("Could not add Control Strategy run status: " + fastRunId);
        } finally {
            session.close();
        }
    }

    // returns control strategy Id for the given name
    public synchronized int isDuplicateFastRunName(String name) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            FastRun cs = dao.getFastRun(name, session);
            return cs == null ? 0 : cs.getId();
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve if FastRun name is already used", e);
            throw new EmfException("Could not retrieve if FastRun name is already used");
        } finally {
            session.close();
        }
    }

    public synchronized int copyFastRun(int id, User creator) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            // get cs to copy
            FastRun cs = dao.getFastRun(id, session);

            session.clear();// clear to flush current

            String name = "Copy of " + cs.getName();
            // make sure this won't cause duplicate issues...
            if (isDuplicateFastRunName(name) != 0)
                throw new EmfException("A control strategy named '" + name + "' already exists.");

            // do a deep copy
            FastRun copied = (FastRun) DeepCopy.copy(cs);
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

//    private synchronized boolean isDuplicate(String name) throws EmfException {
//        return (isDuplicateName(name) != 0);
//    }

    public synchronized FastRun getFastRun(int id) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return dao.getFastRun(id, session);
        } catch (RuntimeException e) {
            LOG.error("Could not get control strategy", e);
            throw new EmfException("Could not get control strategy");
        } finally {
            session.close();
        }
    }

    public synchronized FastRunOutput[] getFastRunOutputs(int fastRunId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List all = dao.getFastRunOutputs(fastRunId, session);
            return (FastRunOutput[]) all.toArray(new FastRunOutput[0]);
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

    public synchronized String getFastRunStatus(int id) throws EmfException {
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

    public FastDataset[] getFastDatasets() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List all = dao.getFastDatasets(session);
            return (FastDataset[]) all.toArray(new FastDataset[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve fast datasets.", e);
            throw new EmfException("Could not retrieve fast datasets.");
        } finally {
            session.close();
        }
    }

    public FastDataset getFastDataset(int fastDatasetId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            FastDataset fastDataset = dao.getFastDataset(session, fastDatasetId);
            return fastDataset;
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve fast datasets.", e);
            throw new EmfException("Could not retrieve fast datasets.");
        } finally {
            session.close();
        }
    }

    public synchronized int addFastDataset(FastDataset fastDataset) throws EmfException {
        Session session = sessionFactory.getSession();
        int csId;
        try {
            csId = dao.addFastDataset(fastDataset, session);
        } catch (RuntimeException e) {
            LOG.error("Could not add FastDataset: " + fastDataset, e);
            throw new EmfException("Could not add FastDataset: " + fastDataset);
        } finally {
            session.close();
        }
        return csId;
    }

    public synchronized void removeFastDataset(int fastDatasetId, User user) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.removeFastDataset(fastDatasetId, session);
        } catch (RuntimeException e) {
            LOG.error("Could not remove FastDataset: " + fastDatasetId, e);
            throw new EmfException("Could not remove FastDataset: " + fastDatasetId);
        } finally {
            session.close();
        }
    }

    public FastNonPointDataset[] getFastNonPointDatasets() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List all = dao.getFastNonPointDatasets(session);
            return (FastNonPointDataset[]) all.toArray(new FastNonPointDataset[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve fast NonPoint datasets.", e);
            throw new EmfException("Could not retrieve fast NonPoint datasets.");
        } finally {
            session.close();
        }
    }

    public FastNonPointDataset getFastNonPointDataset(int fastNonPointDatasetId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            FastNonPointDataset fastNonPointDataset = dao.getFastNonPointDataset(session, fastNonPointDatasetId);
            return fastNonPointDataset;
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve fast NonPoint datasets.", e);
            throw new EmfException("Could not retrieve fast NonPoint datasets.");
        } finally {
            session.close();
        }
    }

    public synchronized int addFastNonPointDataset(FastNonPointDataset fastNonPointDataset) throws EmfException {
        Session session = sessionFactory.getSession();
        int csId;
        try {
            csId = dao.addFastNonPointDataset(fastNonPointDataset, session);
        } catch (RuntimeException e) {
            LOG.error("Could not add FastNonPointDataset: " + fastNonPointDataset, e);
            throw new EmfException("Could not add FastNonPointDataset: " + fastNonPointDataset);
        } finally {
            session.close();
        }
        return csId;
    }

    public synchronized void removeFastNonPointDataset(int fastNonPointDatasetId, User user) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.removeFastNonPointDataset(fastNonPointDatasetId, session);
        } catch (RuntimeException e) {
            LOG.error("Could not remove FastNonPointDataset: " + fastNonPointDatasetId, e);
            throw new EmfException("Could not remove FastNonPointDataset: " + fastNonPointDatasetId);
        } finally {
            session.close();
        }
    }

    public synchronized void createFastQuasiPointDataset(User user, int fastNonPointDatasetId) throws EmfException {
        try {
            CreateFastQuasiPointDatasetTask task = new CreateFastQuasiPointDatasetTask(user,
                    getFastNonPointDataset(fastNonPointDatasetId), sessionFactory, dbServerFactory);
            if (task.shouldProceed())
                threadPool
                        .execute(new GCEnforcerTask("Create Inventories: " + ""/* controlStrategy.getName() */, task));
        } catch (Exception e) {
            LOG.error("Error running control strategy: " + ""/* controlStrategy.getName() */, e);
            throw new EmfException(e.getMessage());
        }
    }

    public Grid[] getGrids() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List all = dao.getGrids(session);
            return (Grid[]) all.toArray(new Grid[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve fast NonPoint datasets.", e);
            throw new EmfException("Could not retrieve fast NonPoint datasets.");
        } finally {
            session.close();
        }
    }

    public Grid getGrid(String name) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            Grid grid = dao.getGrid(session, name);
            return grid;
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve grid, " + name + ".", e);
            throw new EmfException("Could not retrieve grid, " + name + ".");
        } finally {
            session.close();
        }
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public synchronized FastAnalysis[] getFastAnalyses() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List cs = dao.getFastAnalyses(session);
            return (FastAnalysis[]) cs.toArray(new FastAnalysis[0]);
        } catch (HibernateException e) {
            e.printStackTrace();
            LOG.error("Could not retrieve all FastAnalyses.");
            throw new EmfException("Could not retrieve all FastAnalyses.");
        } finally {
            session.close();
        }
    }

    public synchronized int addFastAnalysis(FastAnalysis fastAnalysis) throws EmfException {
        Session session = sessionFactory.getSession();
        int csId;
        try {
            csId = dao.add(fastAnalysis, session);
        } catch (RuntimeException e) {
            e.printStackTrace();
            LOG.error("Could not add Control Strategy: " + fastAnalysis, e);
            throw new EmfException("Could not add Control Strategy: " + fastAnalysis);
        } finally {
            session.close();
        }
        return csId;
    }

    public synchronized void setFastAnalysisRunStatusAndCompletionDate(int id, String runStatus, Date completionDate)
            throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.setFastAnalysisRunStatusAndCompletionDate(id, runStatus, completionDate, session);
        } catch (RuntimeException e) {
            LOG.error("Could not set Control Strategy run status: " + id, e);
            throw new EmfException("Could not add Control Strategy run status: " + id);
        } finally {
            session.close();
        }
    }

    public synchronized FastAnalysis obtainLockedFastAnalysis(User owner, int id) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            FastAnalysis locked = dao.obtainLockedFastAnalysis(owner, id, session);

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
    // public void releaseLocked(FastAnalysis locked) throws EmfException {
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

    public synchronized void releaseLockedFastAnalysis(User user, int id) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.releaseLockedFastAnalysis(user, id, session);
        } catch (RuntimeException e) {
            LOG.error("Could not release lock for Control Strategy id: " + id, e);
            throw new EmfException("Could not release lock for Control Strategy id: " + id);
        } finally {
            session.close();
        }
    }

    public synchronized FastAnalysis updateFastAnalysis(FastAnalysis fastAnalysis) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            if (!dao.canUpdate(fastAnalysis, session))
                throw new EmfException("The Control Strategy name is already in use");

            FastAnalysis released = dao.updateFastAnalysis(fastAnalysis, session);

            return released;
        } catch (RuntimeException e) {
            LOG.error("Could not update Control Strategy: " + fastAnalysis, e);
            throw new EmfException("Could not update FastAnalysis: " + fastAnalysis);
        } finally {
            session.close();
        }
    }

    public synchronized FastAnalysis updateFastAnalysisWithLock(FastAnalysis fastAnalysis) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            if (!dao.canUpdate(fastAnalysis, session))
                throw new EmfException("Control Strategy name already in use");

            FastAnalysis csWithLock = dao.updateWithLock(fastAnalysis, session);

            return csWithLock;
            // return dao.getById(csWithLock.getId(), session);
        } catch (RuntimeException e) {
            LOG.error("Could not update Control Strategy: " + fastAnalysis, e);
            throw new EmfException("Could not update FastAnalysis: " + fastAnalysis);
        } finally {
            session.close();
        }
    }

    // public void removeFastAnalyses(FastAnalysis[] elements, User user) throws EmfException {
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
    // throw new EmfException("Could not update FastAnalysis: " + elements);
    // }
    // }

    public synchronized void removeFastAnalyses(int[] ids, User user) throws EmfException {
        Session session = sessionFactory.getSession();
        String exception = "";
        try {
            for (int i = 0; i < ids.length; i++) {
                FastAnalysis cs = dao.getFastAnalysis(ids[i], session);
                session.clear();

                // check if admin user, then allow it to be removed.
                if (user.equals(cs.getCreator()) || user.isAdmin()) {
                    if (cs.isLocked())
                        exception += "The control strategy, " + cs.getName()
                                + ", is in edit mode and can not be removed. ";
                    else
                        removeFastAnalysis(cs);
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

    public synchronized void removeFastAnalysis(FastAnalysis fastAnalysis) throws EmfException {
        Session session = sessionFactory.getSession();
        try {

            if (!dao.canUpdate(fastAnalysis, session))
                throw new EmfException("Control Strategy name already in use");

            FastAnalysisOutput[] controlStrategyResults = getFastAnalysisOutputs(fastAnalysis.getId());
            for (int i = 0; i < controlStrategyResults.length; i++) {
                dao.remove(controlStrategyResults[i], session);
            }

            dao.remove(fastAnalysis, session);
        } catch (RuntimeException e) {
            LOG.error("Could not remove control strategy: " + fastAnalysis, e);
            throw new EmfException("Could not remove control strategy: " + fastAnalysis.getName());
        } finally {
            session.close();
        }
    }

    public synchronized void runFastAnalysis(User user, int fastAnalysisId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            // first see if the strategy has been canceled, is so don't run it...
            String runStatus = dao.getFastAnalysisRunStatus(fastAnalysisId, session);
            if (runStatus.equals("Cancelled"))
                return;

            FastAnalysis strategy = getFastAnalysis(fastAnalysisId);
            // validateSectors(strategy);
            // get rid of for now, since we don't auto export anything
            // make sure a valid server-side export path was specified
            // validateExportPath(strategy.getExportDirectory());

            // make the runner of the strategy is the owner of the strategy...
            // NEED TO TALK TO ALISON ABOUT ISSUES, LOCEKD owner might not be the creator of resulting datsets,
            // hence a exception when trying to purge/delete the resulting datasets
            // if (control);

            // queue up the strategy to be run, by setting runStatus to Waiting
            dao.setFastAnalysisRunStatusAndCompletionDate(fastAnalysisId, "Waiting", null, session);

            // validatePath(strategy.getExportDirectory());
            RunFastAnalysis runStrategy = new RunFastAnalysis(sessionFactory, dbServerFactory, threadPool);
            runStrategy.run(user, strategy, this);
        } catch (EmfException e) {
            // queue up the strategy to be run, by setting runStatus to Waiting
            dao.setFastAnalysisRunStatusAndCompletionDate(fastAnalysisId, "Failed", null, session);

            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }
    }

    public List<FastAnalysis> getFastAnalysesByRunStatus(String runStatus) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return dao.getFastAnalysesByRunStatus(runStatus, session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not get Control Strategies by run status: " + runStatus);
        } finally {
            session.close();
        }
    }

    public Long getFastAnalysisRunningCount() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return dao.getFastAnalysisRunningCount(session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not get Control Strategies running count");
        } finally {
            session.close();
        }
    }

    // private File validatePath(String folderPath) throws EmfException {
    // File file = new File(folderPath);
    //
    // if (!file.exists() || !file.isDirectory()) {
    // LOG.error("Folder " + folderPath + " does not exist");
    // throw new EmfException("Export folder does not exist: " + folderPath);
    // }
    // return file;
    // }

    public synchronized void stopFastAnalysis(int fastAnalysisId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            // look at the current status, if waiting or running, then update to Cancelled.
            String status = dao.getFastAnalysisRunStatus(fastAnalysisId, session);
            if (status.toLowerCase().startsWith("waiting") || status.toLowerCase().startsWith("running"))
                dao.setFastAnalysisRunStatusAndCompletionDate(fastAnalysisId, "Cancelled", null, session);
        } catch (RuntimeException e) {
            LOG.error("Could not set Control Strategy run status: " + fastAnalysisId, e);
            throw new EmfException("Could not add Control Strategy run status: " + fastAnalysisId);
        } finally {
            session.close();
        }
    }

    // returns control strategy Id for the given name
    public synchronized int isDuplicateFastAnalysisName(String name) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            FastAnalysis cs = dao.getFastAnalysis(name, session);
            return cs == null ? 0 : cs.getId();
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve if FastAnalysis name is already used", e);
            throw new EmfException("Could not retrieve if FastAnalysis name is already used");
        } finally {
            session.close();
        }
    }

    public synchronized int copyFastAnalysis(int id, User creator) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            // get cs to copy
            FastAnalysis cs = dao.getFastAnalysis(id, session);

            session.clear();// clear to flush current

            String name = "Copy of " + cs.getName();
            // make sure this won't cause duplicate issues...
            if (isDuplicateFastAnalysisName(name) != 0)
                throw new EmfException("A control strategy named '" + name + "' already exists.");

            // do a deep copy
            FastAnalysis copied = (FastAnalysis) DeepCopy.copy(cs);
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

    public synchronized FastAnalysis getFastAnalysis(int id) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return dao.getFastAnalysis(id, session);
        } catch (RuntimeException e) {
            LOG.error("Could not get control strategy", e);
            throw new EmfException("Could not get control strategy");
        } finally {
            session.close();
        }
    }

    public synchronized FastAnalysisOutput[] getFastAnalysisOutputs(int fastAnalysisId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List all = dao.getFastAnalysisOutputs(fastAnalysisId, session);
            return (FastAnalysisOutput[]) all.toArray(new FastAnalysisOutput[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve control strategy results.", e);
            throw new EmfException("Could not retrieve control strategy results.");
        } finally {
            session.close();
        }
    }

    public synchronized String getFastAnalysisStatus(int id) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return dao.getFastAnalysisRunStatus(session, id);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve strategy run status.", e);
            throw new EmfException("Could not retrieve strategy run status.");
        } finally {
            session.close();
        }
    }

    public FastAnalysisOutputType getFastAnalysisOutputType(String name) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return dao.getFastAnalysisOutputType(name, session);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve FastAnalysisOutputType.", e);
            throw new EmfException("Could not retrieve FastAnalysisOutputType.");
        } finally {
            session.close();
        }
    }

    public FastAnalysisOutputType[] getFastAnalysisOutputTypes() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List all = dao.getFastAnalysisOutputTypes(session);
            return (FastAnalysisOutputType[]) all.toArray(new FastAnalysisOutputType[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve FastAnalysisOutputType.", e);
            throw new EmfException("Could not retrieve FastAnalysisOutputType.");
        } finally {
            session.close();
        }
    }

    public FastRunOutputType getFastRunOutputType(String name) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return dao.getFastRunOutputType(name, session);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve FastRunOutputType.", e);
            throw new EmfException("Could not retrieve FastRunOutputType.");
        } finally {
            session.close();
        }
    }

    public FastRunOutputType[] getFastRunOutputTypes() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List all = dao.getFastAnalysisOutputTypes(session);
            return (FastRunOutputType[]) all.toArray(new FastRunOutputType[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve FastRunOutputTypes.", e);
            throw new EmfException("Could not retrieve FastRunOutputTypes.");
        } finally {
            session.close();
        }
    }
}
