package gov.epa.emissions.framework.services.qa;

import java.util.Date;

import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfDbServer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.GCEnforcerTask;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class QAServiceImpl implements QAService {

    private static Log LOG = LogFactory.getLog(QAServiceImpl.class);

    private HibernateSessionFactory sessionFactory;

    private QADAO dao;

    private PooledExecutor threadPool;

    public QAServiceImpl() {
        this(HibernateSessionFactory.get());
        this.threadPool = createThreadPool();
    }

    private PooledExecutor createThreadPool() {
        PooledExecutor threadPool = new PooledExecutor(20);
        threadPool.setMinimumPoolSize(1);
        threadPool.setKeepAliveTime(1000 * 60 * 3);// terminate after 3 (unused) minutes

        return threadPool;
    }

    public QAServiceImpl(HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        dao = new QADAO();
    }

    public QAStep[] getQASteps(EmfDataset dataset) throws EmfException {
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

    public QAProgram[] getQAPrograms() throws EmfException {
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

    public void runQAStep(QAStep step, User user) throws EmfException {
        updateWitoutCheckingConstraints(new QAStep[] { step });
        checkRestrictions(step);
        EmfDbServer dbServer = dbServer();

        RunQAStep runner = new RunQAStep(new QAStep[] { step }, user, dbServer, sessionFactory);
        try {
            threadPool.execute(new GCEnforcerTask("Running QA Steps", runner));
        } catch (InterruptedException e) {
            LOG.error("Error running in qa step-" + step.getName(), e);
            throw new EmfException("Error running in qa step-" + step.getName() + ":" + e.getMessage());
        }
    }

    public void exportQAStep(QAStep step, User user, String dirName) throws EmfException {
        EmfDbServer dbServer = dbServer();
        try {
            ExportQAStep exportQATask = new ExportQAStep(step, dbServer, user, sessionFactory, threadPool);
            exportQATask.export(dirName);
        } catch (Exception e) {
            LOG.error("Could not export QA step", e);
            throw new EmfException("Could not export QA step: " + e.getMessage());
        }

    }

    private void checkRestrictions(QAStep step) throws EmfException {
        QAProgram program = step.getProgram();
        if (program == null || !program.getName().startsWith("SQL"))
            throw new EmfException("SQL is the only program currently supported for running a QA Step");
    }

    private EmfDbServer dbServer() throws EmfException {
        EmfDbServer dbServer = null;
        try {
            dbServer = new EmfDbServer();
        } catch (Exception e) {
            LOG.error("Could not get a connection", e);
            throw new EmfException("Could not get a connection-" + e.getMessage());
        }
        return dbServer;
    }

    public void updateWitoutCheckingConstraints(QAStep[] steps) throws EmfException {
        updateIds(steps);
        updateSteps(steps);
    }

    private void updateIds(QAStep[] steps) throws EmfException {
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

    private void updateSteps(QAStep[] steps) throws EmfException {
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

    public void update(QAStep step) throws EmfException {
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

    public QAStepResult getQAStepResult(QAStep step) throws EmfException {
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

    private boolean isCurrentTable(QAStepResult qaStepResult, Session session) {
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

    public QAProgram addQAProgram(QAProgram program) throws EmfException {
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
