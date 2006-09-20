package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfDbServer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.EmfProperty;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;
import java.sql.SQLException;

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
            LOG.error("could not retrieve QA Steps for dataset: " + dataset.getName(), e);
            throw new EmfException("could not retrieve QA Steps for dataset: " + dataset.getName());
        } finally {
            session.close();
        }
    }

    public void update(QAStep[] steps) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.update(steps, session);
        } catch (RuntimeException e) {
            LOG.error("could not update QA Steps", e);
            throw new EmfException("could not update QA Steps");
        } finally {
            session.close();
        }
    }

    public QAProgram[] getQAPrograms() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return dao.getQAPrograms(session);
        } catch (RuntimeException e) {
            LOG.error("could not get QA Programs", e);
            throw new EmfException("could not get QA Programs");
        } finally {
            session.close();
        }
    }

    public void runQAStep(QAStep step, User user) throws EmfException {
        updateQAStepBeforeRun(step);
        checkRestrictions(step);
        EmfDbServer dbServer = dbServer();
        try {
            RunQAStep runner = new RunQAStep(step, user, dbServer, sessionFactory, threadPool);
            runner.run();
        } finally {
            disconnect(dbServer);
        }
    }

    public void exportQAStep(QAStep step, String dirName) throws EmfException {
        exportQAStep(step, dirName, false);
    }

    public void exportQAStepWithOverwrite(QAStep step, String dirName) throws EmfException {
        exportQAStep(step, dirName, true);
    }

    public void exportQAStep(QAStep step, String dirName, boolean overwrite) throws EmfException {
        EmfDbServer dbServer = dbServer();
        try {
            File file = exportFile(dirName, step, overwrite);
            String tableName = step.getTableSource().getTable();
            DatabaseTableCSVExporter exporter = new DatabaseTableCSVExporter(tableName, dbServer
                    .getEmissionsDatasource(), batchSize());
            exporter.export(file);
        } catch (Exception e) {
            LOG.error("could not export QA step", e);
            throw new EmfException("could not export QA step. " + e.getMessage());
        } finally {
            disconnect(dbServer);
        }

    }

    private File exportFile(String dirName, QAStep step, boolean overwrite) throws EmfException {
        File dir = validateDir(dirName);
        String fileName = fileName(step);
        File file = new File(dir, fileName);
        if (!overwrite) {
            if (file.exists() && file.isFile()) {
                LOG.error("File exists and cannot be overwritten");
                throw new EmfException("Cannot export to existing file.  Choose overwrite option");
            }
        }
        return file;
    }

    private String fileName(QAStep qaStep) {
        String result = "QA" + qaStep.getName() + "_DSID" + qaStep.getDatasetId() + "_V" + qaStep.getVersion();

        for (int i = 0; i < result.length(); i++) {
            if (!Character.isJavaLetterOrDigit(result.charAt(i))) {
                result = result.replace(result.charAt(i), '_');
            }
        }

        return result.trim().replaceAll(" ", "_");
    }

    private File validateDir(String dirName) throws EmfException {
        File file = new File(dirName);

        if (!file.exists() || !file.isDirectory()) {
            LOG.error("Folder " + dirName + " does not exist");
            throw new EmfException("Folder does not exist: " + dirName);
        }
        return file;
    }

    private void checkRestrictions(QAStep step) throws EmfException {
        QAProgram program = step.getProgram();
        if (program == null || !program.getName().startsWith("SQL"))
            throw new EmfException("Only SQL program is supported for running a QA Step");
    }

    private EmfDbServer dbServer() throws EmfException {
        EmfDbServer dbServer = null;
        try {
            dbServer = new EmfDbServer();
        } catch (Exception e) {
            LOG.error("could not create EMF db server", e);
            throw new EmfException(e.getMessage());
        }
        return dbServer;
    }

    private void disconnect(EmfDbServer dbServer) throws EmfException {
        try {
            dbServer.disconnect();
        } catch (SQLException e) {
            throw new EmfException("Could not disconnect - " + e.getMessage());

        }
    }

    private void updateQAStepBeforeRun(QAStep step) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.update(new QAStep[] { step }, session);
        } catch (RuntimeException e) {
            LOG.error("could not update QA Step before run", e);
            throw new EmfException("could not update QA Step before run");
        } finally {
            session.close();
        }

    }

    private int batchSize() {
        Session session = sessionFactory.getSession();
        try {
            EmfProperty property = new EmfPropertiesDAO().getProperty("export-batch-size", session);
            return Integer.parseInt(property.getValue());
        } finally {
            session.close();
        }
    }
}
