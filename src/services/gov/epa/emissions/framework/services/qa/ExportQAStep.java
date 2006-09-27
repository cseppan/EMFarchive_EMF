package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.io.Exporter;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfDbServer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.EmfProperty;
import gov.epa.emissions.framework.services.GCEnforcerTask;
import gov.epa.emissions.framework.services.data.EmfDateFormat;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class ExportQAStep {

    private QAStep step;

    private PooledExecutor threadPool;

    private EmfDbServer dbServer;

    private HibernateSessionFactory sessionFactory;

    private User user;

    private static Log LOG = LogFactory.getLog(ExportQAStep.class);

    public ExportQAStep(QAStep step, EmfDbServer dbServer, User user, HibernateSessionFactory sessionFactory,
            PooledExecutor threadPool) {
        this.step = step;
        this.dbServer = dbServer;
        this.user = user;
        this.sessionFactory = sessionFactory;
        this.threadPool = threadPool;
    }

    public void export(String dirName) throws EmfException {
        File file = exportFile(dirName, step);
        Exporter exporter = exporter();
        ExportQAStepTask task = new ExportQAStepTask(file, exporter, dbServer, step, user, sessionFactory);
        try {
            threadPool.execute(new GCEnforcerTask("Export QA Step : " + step.getProgram().getName(), task));
        } catch (InterruptedException e) {
            LOG.error("Error while exporting a qa step: " + step.getName(), e);
            throw new EmfException(e.getMessage());
        }
    }

    private Exporter exporter() throws EmfException {
        String tableName = tableName();
        return new DatabaseTableCSVExporter(tableName, dbServer.getEmissionsDatasource(), batchSize(sessionFactory));
    }

    private String tableName() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            QAStepResult result = new QADAO().qaStepResult(step, session);
            if (result == null)
                throw new EmfException("You have to first run the QA Step before export");
            return result.getTable();
        } finally {
            session.close();
        }
    }

    private File exportFile(String dirName, QAStep step) throws EmfException {
        File dir = validateDir(dirName);
        String fileName = fileName(step);
        File file = new File(dir, fileName);
        return file;
    }

    private String fileName(QAStep qaStep) {
        String formattedDate = EmfDateFormat.format_ddMMMyyyy(new Date());
        String result = "QA" + qaStep.getName() + "_DSID" + qaStep.getDatasetId() + "_V" + qaStep.getVersion() + "_"
                + formattedDate;

        for (int i = 0; i < result.length(); i++) {
            if (!Character.isJavaLetterOrDigit(result.charAt(i))) {
                result = result.replace(result.charAt(i), '_');
            }
        }

        return result.trim().replaceAll(" ", "_") + ".csv";
    }

    private File validateDir(String dirName) throws EmfException {
        File file = new File(dirName);

        if (!file.exists() || !file.isDirectory()) {
            LOG.error("Folder " + dirName + " does not exist");
            throw new EmfException("Folder does not exist: " + dirName);
        }
        return file;
    }

    private int batchSize(HibernateSessionFactory sessionFactory) {
        Session session = sessionFactory.getSession();
        try {
            EmfProperty property = new EmfPropertiesDAO().getProperty("export-batch-size", session);
            return Integer.parseInt(property.getValue());
        } finally {
            session.close();
        }
    }

}
