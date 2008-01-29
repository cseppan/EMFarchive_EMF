package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.TableCreator;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.GCEnforcerTask;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class QAServiceImpl implements QAService {

    private static Log LOG = LogFactory.getLog(QAServiceImpl.class);

    private HibernateSessionFactory sessionFactory;

    private DbServerFactory dbServerFactory;

    private QADAO dao;

    private PooledExecutor threadPool;

    public QAServiceImpl() {
        this(HibernateSessionFactory.get(), DbServerFactory.get());
    }

    public QAServiceImpl(HibernateSessionFactory sessionFactory) {
        this(sessionFactory, DbServerFactory.get());
    }
    
    public QAServiceImpl(HibernateSessionFactory sessionFactory, DbServerFactory dbServerFactory) {
        this.sessionFactory = sessionFactory;
        this.dbServerFactory = dbServerFactory;
        this.threadPool = createThreadPool();
        dao = new QADAO();
    }
    
    private synchronized PooledExecutor createThreadPool() {
        PooledExecutor threadPool = new PooledExecutor(20);
        threadPool.setMinimumPoolSize(1);
        threadPool.setKeepAliveTime(1000 * 60 * 3);// terminate after 3 (unused) minutes

        return threadPool;
    }

    public synchronized QAStep[] getQASteps(EmfDataset dataset) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            QAStep[] results = dao.steps(dataset, session);
            return results;
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve QA Steps for dataset: " + dataset.getName(), e);
            throw new EmfException("Could not retrieve QA Steps for dataset: " + dataset.getName());
        } finally {
            session.close();
        }
    }

    public synchronized QAProgram[] getQAPrograms() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return dao.getQAPrograms(session);
        } catch (RuntimeException e) {
            LOG.error("Could not get QA Programs", e);
            throw new EmfException("Could not get QA Programs");
        } finally {
            session.close();
        }
    }

    public synchronized void runQAStep(QAStep step, User user) throws EmfException {
        updateResultStatus(step, "In process");
        updateWitoutCheckingConstraints(new QAStep[] { step });
        checkRestrictions(step);
        DbServer dbServer = dbServerFactory.getDbServer();
        removeQAResultTable(step, dbServer);

        RunQAStep runner = new RunQAStep(new QAStep[] { step }, user, 
                dbServerFactory, sessionFactory);
        try {
            threadPool.execute(new GCEnforcerTask("Running QA Steps", runner));
        } catch (Exception e) {
            LOG.error("Error running in qa step-" + step.getName(), e);
            throw new EmfException("Error running in qa step-" + step.getName() + ":" + e.getMessage());
        }
    }

    private synchronized void removeQAResultTable(QAStep step, DbServer dbServer) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            QAStepResult result = dao.qaStepResult(step, session);
            
            if (result == null)
                return;
            
            String table = result.getTable();
            
            if (table != null && !table.trim().isEmpty()) {
                TableCreator tableCreator = new TableCreator(dbServer.getEmissionsDatasource());
                
                if (tableCreator.exists(table.trim())) {
                    tableCreator.drop(table.trim());
                }
            }
            
            dao.removeQAStepResult(result, session);
        } catch (Exception e) {
            LOG.error("Cannot drop result table for QA step: " + step.getName(), e);
            throw new EmfException("Cannot drop result table for QA step: " + step.getName());
        } finally {
            try {
                session.close();
                
                if (dbServer != null && dbServer.isConnected())
                    dbServer.disconnect();
            } catch (Exception e) {
                throw new EmfException(e.getMessage());
            }
        }
    }

    private synchronized void updateResultStatus(QAStep qaStep, String status) {
        Session session = sessionFactory.getSession();
        try {
            QAStepResult result = dao.qaStepResult(qaStep, session);

            if (result == null)
                return;

            result.setTableCreationStatus(status);
            dao.updateQAStepResult(result, session);
        } finally {
            session.close();
        }
    }

    public synchronized void exportQAStep(QAStep step, User user, String dirName) throws EmfException {
        try {
            ExportQAStep exportQATask = new ExportQAStep(step, dbServerFactory, user, sessionFactory, threadPool);
            exportQATask.export(dirName);
        } catch (Exception e) {
            LOG.error("Could not export QA step", e);
            throw new EmfException("Could not export QA step: " + e.getMessage());
        }
    }

    private synchronized void checkRestrictions(QAStep step) throws EmfException {
        QAProgram program = step.getProgram();
        if (program == null)
            throw new EmfException("Please specify a runnable QA program before running (e.g., SQL)");
        String runClassName = program.getRunClassName();
        if ((runClassName == null) || (runClassName.trim().length() == 0))
            throw new EmfException("The program " + program.getName() + " cannot currently be run in the EMF");
    }

    public synchronized void updateWitoutCheckingConstraints(QAStep[] steps) throws EmfException {
        updateIds(steps);
        updateSteps(steps);
    }

    private synchronized void updateIds(QAStep[] steps) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.updateQAStepsIds(steps, session);
        } catch (RuntimeException e) {
            LOG.error("Could not set the ids", e);
            throw new EmfException("Could not set the ids");
        } finally {
            session.close();
        }

    }

    private synchronized void updateSteps(QAStep[] steps) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.update(steps, session);
        } catch (RuntimeException e) {
            LOG.error("Could not update QA Steps", e);
            throw new EmfException("Could not update QA Steps");
        } finally {
            session.close();
        }
    }

    public synchronized void update(QAStep step) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            if (dao.exists(step, session)) {
                throw new EmfException("The selected QA Step name is already in use");
            }
            dao.update(new QAStep[] { step }, session);
        } catch (RuntimeException e) {
            LOG.error("Could not update QA Step", e);
            throw new EmfException("Could not update QA Step -" + e.getMessage());
        } finally {
            session.close();
        }

    }

    public synchronized QAStepResult getQAStepResult(QAStep step) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            QAStepResult qaStepResult = dao.qaStepResult(step, session);
            if (qaStepResult != null)
                qaStepResult.setCurrentTable(isCurrentTable(qaStepResult, session));
            return qaStepResult;
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve QA Step Result", e);
            throw new EmfException("Could not retrieve QA Step Result");
        } finally {
            session.close();
        }
    }

    private synchronized boolean isCurrentTable(QAStepResult qaStepResult, Session session) {
        Version version = new Versions().get(qaStepResult.getDatasetId(), qaStepResult.getVersion(), session);
        Date versionDate = version.getLastModifiedDate();
        Date date = qaStepResult.getTableCreationDate();
        if (date == null || versionDate == null)
            return false;
        int value = date.compareTo(versionDate);
        if (value >= 0)
            return true;

        return false;

    }

    public synchronized QAProgram addQAProgram(QAProgram program) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return dao.addQAProgram(program, session);
        } catch (RuntimeException e) {
            LOG.error("Could not add QA Program " + program.getName() + ". ", e);
            throw new EmfException("Could not add QA Program " + program.getName() + ".");
        } finally {
            session.close();
        }
    }

}
