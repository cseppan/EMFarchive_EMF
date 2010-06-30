package gov.epa.emissions.framework.services.fast;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.io.DeepCopy;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.GCEnforcerTask;
import gov.epa.emissions.framework.services.basic.UserDAO;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
        dao = new FastDAO(dbServerFactory, sessionFactory);
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

    private <T> T executeDaoCommand(AbstractDaoCommand<T> daoCommand) throws EmfException {

        daoCommand.setSessionFactory(this.sessionFactory);
        daoCommand.setLog(LOG);
        return (T) daoCommand.execute().getReturnValue();
    }

    public synchronized FastRun[] getFastRuns() throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<FastRun[]>() {
            @Override
            protected void doExecute(Session session) throws Exception {
                List cs = dao.getFastRuns(session);
                this.setReturnValue((FastRun[]) cs.toArray(new FastRun[0]));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not retrieve all FastRuns.";
            }
        });
    }

    public synchronized int addFastRun(final FastRun fastRun) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<Integer>() {
            @Override
            protected void doExecute(Session session) throws Exception {
                this.setReturnValue(dao.add(fastRun, session));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not add Control Strategy: " + fastRun;
            }
        });
    }

    public synchronized void setFastRunRunStatusAndCompletionDate(final int id, final String runStatus,
            final Date completionDate) throws EmfException {

        this.executeDaoCommand(new AbstractDaoCommand<Void>() {
            @Override
            protected void doExecute(Session session) throws Exception {
                dao.setFastRunRunStatusAndCompletionDate(id, runStatus, completionDate, session);
            }

            @Override
            protected String getErrorMessage() {
                return "Could not set Control Strategy run status: " + id;
            }
        });
    }

    public synchronized FastRun obtainLockedFastRun(final User owner, final int id) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<FastRun>() {
            @Override
            protected void doExecute(Session session) throws Exception {

                FastRun locked = dao.obtainLockedFastRun(owner, id, session);
                this.setReturnValue(locked);
            }

            @Override
            protected String getErrorMessage() {
                return "Could not obtain lock for Control Strategy: id = " + id + " by owner: " + owner.getUsername();
            }
        });
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

    public synchronized void releaseLockedFastRun(final User user, final int id) throws EmfException {

        this.executeDaoCommand(new AbstractDaoCommand<Void>() {
            @Override
            protected void doExecute(Session session) throws Exception {
                dao.releaseLockedFastRun(user, id, session);
            }

            @Override
            protected String getErrorMessage() {
                return "Could not release lock for Control Strategy id: " + id;
            }
        });
    }

    public synchronized FastRun updateFastRun(final FastRun fastRun) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<FastRun>() {
            @Override
            protected void doExecute(Session session) throws Exception {

                if (!dao.canUpdateFastRun(fastRun, session)) {
                    throw new EmfException("The Control Strategy name is already in use");
                }

                FastRun released = dao.updateFastRun(fastRun, session);

                this.setReturnValue(released);
            }

            @Override
            protected String getErrorMessage() {
                return "Could not update Control Strategy: " + fastRun;
            }
        });
    }

    public synchronized FastRun updateFastRunWithLock(final FastRun fastRun) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<FastRun>() {
            @Override
            protected void doExecute(Session session) throws Exception {

                if (!dao.canUpdateFastRun(fastRun, session)) {
                    throw new EmfException("Control Strategy name already in use");
                }

                FastRun csWithLock = dao.updateFastRunWithLock(fastRun, session);

                this.setReturnValue(csWithLock);
            }

            @Override
            protected String getErrorMessage() {
                return "Could not update Control Strategy: " + fastRun;
            }
        });
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

    public synchronized void removeFastRuns(final int[] ids, final User user) throws EmfException {

        this.executeDaoCommand(new AbstractDaoCommand<Void>() {
            @Override
            protected void doExecute(Session session) throws Exception {

                String exception = "";
                for (int i = 0; i < ids.length; i++) {
                    FastRun cs = dao.getFastRun(ids[i], session);
                    session.clear();

                    // check if admin user, then allow it to be removed.
                    if (user.equals(cs.getCreator()) || user.isAdmin()) {
                        if (cs.isLocked()) {
                            exception += "The control strategy, " + cs.getName()
                                    + ", is in edit mode and can not be removed. ";
                        } else {
                            removeFastRun(cs);
                        }
                    } else {
                        exception += "You do not have permission to remove the strategy: " + cs.getName() + ". ";
                    }
                }

                if (exception.length() > 0) {
                    throw new EmfException(exception);
                }
            }

            @Override
            protected String getErrorMessage() {
                return "Could not remove Control Strategy";
            }
        });
    }

    private synchronized void removeFastRun(final FastRun fastRun) throws EmfException {

        this.executeDaoCommand(new AbstractDaoCommand<Void>() {
            @Override
            protected void doExecute(Session session) throws Exception {

                if (!dao.canUpdateFastRun(fastRun, session))
                    throw new EmfException("Control Strategy name already in use");

                FastRunOutput[] controlStrategyResults = getFastRunOutputs(fastRun.getId());
                for (int i = 0; i < controlStrategyResults.length; i++) {
                    dao.remove(controlStrategyResults[i], session);
                }

                dao.remove(fastRun, session);
            }

            @Override
            protected String getErrorMessage() {
                return "Could not remove control strategy: " + fastRun;
            }
        });
    }

    public synchronized void removeResultDatasets(final Integer[] ids, final User user) throws EmfException {

        this.executeDaoCommand(new AbstractDaoCommand<Void>() {
            @Override
            protected void doExecute(Session session) throws Exception {

                DatasetDAO dsDao = new DatasetDAO();
                for (Integer id : ids) {
                    EmfDataset dataset = dsDao.getDataset(session, id);

                    if (dataset != null) {
                        try {
                            dsDao.remove(user, dataset, session);
                        } catch (EmfException e) {

                            if (DebugLevels.DEBUG_12) {
                                System.out.println(e.getMessage());
                            }

                            throw new EmfException(e.getMessage());
                        }
                    }
                }
            }

            @Override
            protected String getErrorMessage() {
                return "";
            }
        });
    }

    public synchronized void runFastRun(final User user, final int fastRunId) throws EmfException {

        this.executeDaoCommand(new AbstractDaoCommand<Void>() {
            @Override
            protected void doExecute(Session session) throws Exception {

                try {

                    // first see if the strategy has been canceled, is so don't run it...
                    String runStatus = dao.getFastRunRunStatus(fastRunId, session);
                    if (runStatus.equals("Cancelled")) {
                        return;
                    }

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
                    runStrategy.run(user, strategy, FastServiceImpl.this);
                } catch (EmfException e) {

                    // queue up the strategy to be run, by setting runStatus to Waiting
                    dao.setFastRunRunStatusAndCompletionDate(fastRunId, "Failed", null, session);

                    throw new EmfException(e.getMessage());
                }
            }

            @Override
            protected String getErrorMessage() {
                return "";
            }
        });
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

    public List<FastRun> getFastRunsByRunStatus(final String runStatus) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<List<FastRun>>() {
            @Override
            protected void doExecute(Session session) throws Exception {
                this.setReturnValue(dao.getFastRunsByRunStatus(runStatus, session));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not get Control Strategies by run status: " + runStatus;
            }
        });
    }

    public Long getFastRunRunningCount() throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<Long>() {
            @Override
            protected void doExecute(Session session) throws Exception {
                this.setReturnValue(dao.getFastRunRunningCount(session));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not get Control Strategies running count";
            }
        });
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

    public synchronized void stopFastRun(final int fastRunId) throws EmfException {

        this.executeDaoCommand(new AbstractDaoCommand<Void>() {
            @Override
            protected void doExecute(Session session) throws Exception {
                // look at the current status, if waiting or running, then update to Cancelled.
                String status = dao.getFastRunRunStatus(fastRunId, session);
                if (status.toLowerCase().startsWith("waiting") || status.toLowerCase().startsWith("running")) {
                    dao.setFastRunRunStatusAndCompletionDate(fastRunId, "Cancelled", null, session);
                }
            }

            @Override
            protected String getErrorMessage() {
                return "Could not add Control Strategy run status: " + fastRunId;
            }
        });
    }

    // returns control strategy Id for the given name
    public synchronized int isDuplicateFastRunName(final String name) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<Integer>() {
            @Override
            protected void doExecute(Session session) throws Exception {

                FastRun cs = dao.getFastRun(name, session);
                this.setReturnValue(cs == null ? 0 : cs.getId());
            }

            @Override
            protected String getErrorMessage() {
                return "Could not retrieve if FastRun name is already used";
            }
        });
    }

    public synchronized int copyFastRun(final int id, final User creator) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<Integer>() {
            @Override
            protected void doExecute(Session session) throws Exception {
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
                this.setReturnValue(copied.getId());
            }

            @Override
            protected String getErrorMessage() {
                return "Could not copy control strategy";
            }
        });
    }

    // private synchronized boolean isDuplicate(String name) throws EmfException {
    // return (isDuplicateName(name) != 0);
    // }

    public synchronized FastRun getFastRun(final int id) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<FastRun>() {
            @Override
            protected void doExecute(Session session) throws Exception {
                this.setReturnValue(dao.getFastRun(id, session));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not get control strategy";
            }
        });
    }

    public synchronized FastRunOutput[] getFastRunOutputs(final int fastRunId) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<FastRunOutput[]>() {
            @Override
            protected void doExecute(Session session) throws Exception {
                List<FastRunOutput> all = dao.getFastRunOutputs(fastRunId, session);
                this.setReturnValue(all.toArray(new FastRunOutput[0]));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not retrieve control strategy results";
            }
        });
    }

    public synchronized String getDefaultExportDirectory() throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<String>() {
            @Override
            protected void doExecute(Session session) throws Exception {
                this.setReturnValue(dao.getDefaultExportDirectory(session));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not retrieve default export directory";
            }
        });
    }

    public synchronized String getFastRunStatus(final int id) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<String>() {
            @Override
            protected void doExecute(Session session) throws Exception {
                this.setReturnValue(dao.getStrategyRunStatus(session, id));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not retrieve strategy run status";
            }
        });
    }

    public FastDataset[] getFastDatasets() throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<FastDataset[]>() {
            @Override
            protected void doExecute(Session session) throws Exception {
                List<FastDataset> all = dao.getFastDatasets(session);
                this.setReturnValue(all.toArray(new FastDataset[0]));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not retrieve fast datasets";
            }
        });
    }

    public FastDataset getFastDataset(final int fastDatasetId) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<FastDataset>() {
            @Override
            protected void doExecute(Session session) throws Exception {
                this.setReturnValue(dao.getFastDataset(session, fastDatasetId));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not retrieve fast datasets";
            }
        });
    }

    public int getFastDatasetCount() throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<Integer>() {
            @Override
            protected void doExecute(Session session) throws Exception {
                List<FastDataset> all = dao.getFastDatasets(session);
                this.setReturnValue(all.size());
            }

            @Override
            protected String getErrorMessage() {
                return "Could not retrieve fast datasets";
            }
        });
    }

    public synchronized int addFastDataset(final FastDataset fastDataset) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<Integer>() {
            @Override
            protected void doExecute(Session session) throws Exception {
                this.setReturnValue(dao.addFastDataset(fastDataset, session));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not add FastDataset: " + fastDataset;
            }
        });
    }

    // public synchronized int addFastNonPointDataset(final String newInventoryDatasetName,
    // final String baseNonPointDatasetName, final int baseNonPointDatasetVersion,
    // final String griddedSMKDatasetName, final int griddedSMKDatasetVersion, final String invTableDatasetName,
    // final int invTableDatasetVersion, final String gridName, final String userName) throws EmfException {
    //
    // return this.executeDaoCommand(new AbstractDaoCommand<Integer>() {
    // @Override
    // protected void doExecute(Session session) throws Exception {
    //
    // DbServer dbServer = dbServerFactory.getDbServer();
    //
    // int fastDatasetId = dao.addFastNonPointDataset(newInventoryDatasetName, baseNonPointDatasetName,
    // baseNonPointDatasetVersion, griddedSMKDatasetName, griddedSMKDatasetVersion,
    // invTableDatasetName, invTableDatasetVersion, gridName, userName, session, dbServer);
    //
    // populateFastQuasiPointDataset((new UserDAO()).get(userName, session), fastDatasetId);
    //
    // this.setReturnValue(fastDatasetId);
    // }
    //
    // @Override
    // protected String getErrorMessage() {
    // return "Could not add FastDataset: " + newInventoryDatasetName;
    // }
    // });
    // }

    public synchronized void removeFastDataset(final int fastDatasetId, User user) throws EmfException {

        this.executeDaoCommand(new AbstractDaoCommand<Void>() {
            @Override
            protected void doExecute(Session session) throws Exception {
                dao.removeFastDataset(fastDatasetId, session);
            }

            @Override
            protected String getErrorMessage() {
                return "Could not remove FastDataset: " + fastDatasetId;
            }
        });
    }

    private synchronized void close(final DbServer dbServer) throws EmfException {

        this.executeDaoCommand(new AbstractDaoCommand<Void>() {
            @Override
            protected void doExecute(Session session) throws Exception {
                if (dbServer != null) {
                    dbServer.disconnect();
                }
            }

            @Override
            protected String getErrorMessage() {
                return "Could not close database server";
            }
        });
    }

    public FastNonPointDataset[] getFastNonPointDatasets() throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<FastNonPointDataset[]>() {
            @Override
            protected void doExecute(Session session) throws Exception {
                List<FastNonPointDataset> all = dao.getFastNonPointDatasets(session);
                this.setReturnValue(all.toArray(new FastNonPointDataset[0]));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not retrieve fast NonPoint datasets";
            }
        });
    }

    public FastNonPointDataset getFastNonPointDataset(final int fastNonPointDatasetId) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<FastNonPointDataset>() {
            @Override
            protected void doExecute(Session session) throws Exception {
                this.setReturnValue(dao.getFastNonPointDataset(session, fastNonPointDatasetId));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not retrieve fast NonPoint datasets";
            }
        });
    }

    public int getFastNonPointDatasetCount() throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<Integer>() {
            @Override
            protected void doExecute(Session session) throws Exception {
                List<FastNonPointDataset> all = dao.getFastNonPointDatasets(session);
                this.setReturnValue(all.size());
            }

            @Override
            protected String getErrorMessage() {
                return "Could not retrieve fast datasets";
            }
        });
    }

    public synchronized int addFastNonPointDataset(final FastNonPointDataset fastNonPointDataset, final User user)
            throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<Integer>() {
            @Override
            protected void doExecute(Session session) throws Exception {

                DbServer dbServer = dbServerFactory.getDbServer();

                int fastDatasetId = dao.addFastNonPointDataset(fastNonPointDataset.getName(), fastNonPointDataset
                        .getBaseNonPointDataset().getName(), fastNonPointDataset.getBaseNonPointDatasetVersion(),
                        fastNonPointDataset.getGriddedSMKDataset().getName(), fastNonPointDataset
                                .getGriddedSMKDatasetVersion(), fastNonPointDataset.getInvTableDataset().getName(),
                        fastNonPointDataset.getInvTableDatasetVersion(), fastNonPointDataset.getGrid().getName(), user
                                .getName(), session, dbServer);

                populateFastQuasiPointDataset((new UserDAO()).get(user.getName(), session), fastDatasetId);

                this.setReturnValue(fastDatasetId);
            }

            @Override
            protected String getErrorMessage() {
                return "Could not add FastNonPointDataset: " + fastNonPointDataset;
            }
        });
    }

    public synchronized void removeFastNonPointDataset(final int fastNonPointDatasetId, User user) throws EmfException {

        this.executeDaoCommand(new AbstractDaoCommand<Void>() {
            @Override
            protected void doExecute(Session session) throws Exception {
                dao.removeFastNonPointDataset(fastNonPointDatasetId, session);
            }

            @Override
            protected String getErrorMessage() {
                return "Could not remove FastNonPointDataset: " + fastNonPointDatasetId;
            }
        });
    }

    private void populateFastQuasiPointDataset(final User user, final int fastNonPointDatasetId) throws EmfException {
        this.executeDaoCommand(new AbstractDaoCommand<Void>() {
            @Override
            protected void doExecute(Session session) throws Exception {

                FastDataset fastDataset = getFastDataset(fastNonPointDatasetId);
                PopulateFastQuasiPointDatasetTask task = new PopulateFastQuasiPointDatasetTask(user, fastDataset,
                        sessionFactory, dbServerFactory);
                if (task.shouldProceed()) {
                    threadPool.execute(new GCEnforcerTask("Populate FAST Quasi Point Inventory: "
                            + fastDataset.getDataset().getName(), task));
                }
            }

            @Override
            protected String getErrorMessage() {
                return "Error running control strategy: " + ""/* controlStrategy.getName() */;
            }
        });
    }

    public Grid[] getGrids() throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<Grid[]>() {
            @Override
            protected void doExecute(Session session) throws Exception {
                List<Grid> all = dao.getGrids(session);
                this.setReturnValue(all.toArray(new Grid[0]));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not retrieve fast NonPoint datasets";
            }
        });
    }

    public Grid getGrid(final String name) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<Grid>() {
            @Override
            protected void doExecute(Session session) throws Exception {
                this.setReturnValue(dao.getGrid(session, name));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not retrieve grid " + name;
            }
        });
    }

    public synchronized FastAnalysis[] getFastAnalyses() throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<FastAnalysis[]>() {
            @Override
            protected void doExecute(Session session) throws Exception {
                List<FastAnalysis> cs = dao.getFastAnalyses(session);
                this.setReturnValue(cs.toArray(new FastAnalysis[0]));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not retrieve all FastAnalyses";
            }
        });
    }

    public synchronized int addFastAnalysis(final FastAnalysis fastAnalysis) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<Integer>() {
            @Override
            protected void doExecute(Session session) throws Exception {
                this.setReturnValue(dao.add(fastAnalysis, session));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not add Control Strategy: " + fastAnalysis;
            }
        });
    }

    public synchronized void setFastAnalysisRunStatusAndCompletionDate(final int id, final String runStatus,
            final Date completionDate) throws EmfException {

        this.executeDaoCommand(new AbstractDaoCommand<Void>() {
            @Override
            protected void doExecute(Session session) throws Exception {
                dao.setFastAnalysisRunStatusAndCompletionDate(id, runStatus, completionDate, session);
            }

            @Override
            protected String getErrorMessage() {
                return "Could not set Control Strategy run status: " + id;
            }
        });
    }

    public synchronized FastAnalysis obtainLockedFastAnalysis(final User owner, final int id) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<FastAnalysis>() {
            @Override
            protected void doExecute(Session session) throws Exception {
                this.setReturnValue(dao.obtainLockedFastAnalysis(owner, id, session));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not obtain lock for Control Strategy: id = " + id + " by owner: " + owner.getUsername();
            }
        });
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

    public synchronized void releaseLockedFastAnalysis(final User user, final int id) throws EmfException {

        this.executeDaoCommand(new AbstractDaoCommand<Void>() {
            @Override
            protected void doExecute(Session session) throws Exception {
                dao.releaseLockedFastAnalysis(user, id, session);
            }

            @Override
            protected String getErrorMessage() {
                return "Could not release lock for Control Strategy id: " + id;
            }
        });
    }

    public synchronized FastAnalysis updateFastAnalysis(final FastAnalysis fastAnalysis) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<FastAnalysis>() {
            @Override
            protected void doExecute(Session session) throws Exception {

                if (!dao.canUpdate(fastAnalysis, session)) {
                    throw new EmfException("The Control Strategy name is already in use");
                }

                this.setReturnValue(dao.updateFastAnalysis(fastAnalysis, session));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not update Control Strategy: " + fastAnalysis;
            }
        });
    }

    public synchronized FastAnalysis updateFastAnalysisWithLock(final FastAnalysis fastAnalysis) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<FastAnalysis>() {
            @Override
            protected void doExecute(Session session) throws Exception {
                if (!dao.canUpdate(fastAnalysis, session)) {
                    throw new EmfException("Control Strategy name already in use");
                }

                this.setReturnValue(dao.updateWithLock(fastAnalysis, session));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not update Control Strategy: " + fastAnalysis;
            }
        });
    }

    public synchronized void removeFastAnalyses(final int[] ids, final User user) throws EmfException {

        this.executeDaoCommand(new AbstractDaoCommand<Void>() {
            @Override
            protected void doExecute(Session session) throws Exception {

                String exception = "";
                for (int i = 0; i < ids.length; i++) {
                    FastAnalysis cs = dao.getFastAnalysis(ids[i], session);
                    session.clear();

                    // check if admin user, then allow it to be removed.
                    if (user.equals(cs.getCreator()) || user.isAdmin()) {
                        if (cs.isLocked()) {
                            exception += "The control strategy, " + cs.getName()
                                    + ", is in edit mode and can not be removed. ";
                        } else {
                            removeFastAnalysis(cs);
                        }
                    } else {
                        exception += "You do not have permission to remove the strategy: " + cs.getName() + ". ";
                    }
                }

                if (exception.length() > 0) {
                    throw new EmfException(exception);
                }
            }

            @Override
            protected String getErrorMessage() {
                return "Could not remove Control Strategy";
            }
        });
    }

    public synchronized void removeFastAnalysis(final FastAnalysis fastAnalysis) throws EmfException {

        this.executeDaoCommand(new AbstractDaoCommand<Void>() {
            @Override
            protected void doExecute(Session session) throws Exception {
                if (!dao.canUpdate(fastAnalysis, session)) {
                    throw new EmfException("Control Strategy name already in use");
                }

                FastAnalysisOutput[] controlStrategyResults = getFastAnalysisOutputs(fastAnalysis.getId());
                for (int i = 0; i < controlStrategyResults.length; i++) {
                    dao.remove(controlStrategyResults[i], session);
                }

                dao.remove(fastAnalysis, session);
            }

            @Override
            protected String getErrorMessage() {
                return "Could not remove control strategy: " + fastAnalysis;
            }
        });
    }

    public synchronized void runFastAnalysis(final User user, final int fastAnalysisId) throws EmfException {

        this.executeDaoCommand(new AbstractDaoCommand<Void>() {
            @Override
            protected void doExecute(Session session) throws Exception {

                try {
                    // first see if the strategy has been canceled, is so don't run it...
                    String runStatus = dao.getFastAnalysisRunStatus(fastAnalysisId, session);
                    if (runStatus.equals("Cancelled")) {
                        return;
                    }

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
                    runStrategy.run(user, strategy, FastServiceImpl.this);
                } finally {
                    // queue up the strategy to be run, by setting runStatus to Waiting
                    dao.setFastAnalysisRunStatusAndCompletionDate(fastAnalysisId, "Failed", null, session);
                }
            }
        });
    }

    public List<FastAnalysis> getFastAnalysesByRunStatus(final String runStatus) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<List<FastAnalysis>>() {
            @Override
            protected void doExecute(Session session) throws Exception {
                this.setReturnValue(dao.getFastAnalysesByRunStatus(runStatus, session));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not get Control Strategies by run status: " + runStatus;
            }
        });
    }

    public Long getFastAnalysisRunningCount() throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<Long>() {
            @Override
            protected void doExecute(Session session) throws Exception {
                this.setReturnValue(dao.getFastAnalysisRunningCount(session));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not get Control Strategies running count";
            }
        });
    }

    public synchronized void stopFastAnalysis(final int fastAnalysisId) throws EmfException {

        this.executeDaoCommand(new AbstractDaoCommand<Void>() {
            @Override
            protected void doExecute(Session session) throws Exception {
                // look at the current status, if waiting or running, then update to Cancelled.
                String status = dao.getFastAnalysisRunStatus(fastAnalysisId, session);
                if (status.toLowerCase().startsWith("waiting") || status.toLowerCase().startsWith("running")) {
                    dao.setFastAnalysisRunStatusAndCompletionDate(fastAnalysisId, "Cancelled", null, session);
                }
            }

            @Override
            protected String getErrorMessage() {
                return "Could not add Control Strategy run status: " + fastAnalysisId;
            }
        });
    }

    // returns control strategy Id for the given name
    public synchronized int isDuplicateFastAnalysisName(final String name) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<Integer>() {
            @Override
            protected void doExecute(Session session) throws Exception {
                FastAnalysis cs = dao.getFastAnalysis(name, session);
                this.setReturnValue(cs == null ? 0 : cs.getId());
            }

            @Override
            protected String getErrorMessage() {
                return "Could not retrieve if FastAnalysis name is already used";
            }
        });
    }

    public synchronized int copyFastAnalysis(final int id, final User creator) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<Integer>() {
            @Override
            protected void doExecute(Session session) throws Exception {

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
                this.setReturnValue(copied.getId());
            }

            @Override
            protected String getErrorMessage() {
                return "Could not copy control strategy";
            }
        });
    }

    public synchronized FastAnalysis getFastAnalysis(final int id) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<FastAnalysis>() {
            @Override
            protected void doExecute(Session session) throws Exception {
                this.setReturnValue(dao.getFastAnalysis(id, session));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not get control strategy";
            }
        });
    }

    public synchronized FastAnalysisOutput[] getFastAnalysisOutputs(final int fastAnalysisId) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<FastAnalysisOutput[]>() {
            @Override
            protected void doExecute(Session session) throws Exception {
                List<FastAnalysisOutput> all = dao.getFastAnalysisOutputs(fastAnalysisId, session);
                this.setReturnValue(all.toArray(new FastAnalysisOutput[0]));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not retrieve control strategy results";
            }
        });
    }

    public synchronized String getFastAnalysisStatus(final int id) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<String>() {
            @Override
            protected void doExecute(Session session) throws Exception {
                this.setReturnValue(dao.getFastAnalysisRunStatus(session, id));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not retrieve strategy run status";
            }
        });
    }

    public FastAnalysisOutputType getFastAnalysisOutputType(final String name) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<FastAnalysisOutputType>() {
            @Override
            protected void doExecute(Session session) throws Exception {
                this.setReturnValue(dao.getFastAnalysisOutputType(name, session));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not retrieve FastAnalysisOutputType";
            }
        });
    }

    public FastAnalysisOutputType[] getFastAnalysisOutputTypes() throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<FastAnalysisOutputType[]>() {
            @Override
            protected void doExecute(Session session) throws Exception {
                List<FastAnalysisOutputType> all = dao.getFastAnalysisOutputTypes(session);
                this.setReturnValue(all.toArray(new FastAnalysisOutputType[0]));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not retrieve FastAnalysisOutputType";
            }
        });
    }

    public FastRunOutputType getFastRunOutputType(final String name) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<FastRunOutputType>() {
            @Override
            protected void doExecute(Session session) throws Exception {
                this.setReturnValue(dao.getFastRunOutputType(name, session));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not retrieve FastRunOutputType";
            }
        });
    }

    public FastRunOutputType[] getFastRunOutputTypes() throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<FastRunOutputType[]>() {
            @Override
            protected void doExecute(Session session) throws Exception {
                List<FastRunOutputType> all = dao.getFastRunOutputTypes(session);
                this.setReturnValue(all.toArray(new FastRunOutputType[0]));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not retrieve FastRunOutputTypes";
            }
        });
    }
}
