package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.TableCreator;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.Date;

import org.hibernate.Session;

public class SQLQAProgramRunner implements QAProgramRunner {

    private DbServer dbServer;

    private QAStep qaStep;

    private TableCreator tableCreator;

    private HibernateSessionFactory sessionFactory;

    public SQLQAProgramRunner(DbServer dbServer, HibernateSessionFactory sessionFactory, QAStep qaStep) {
        this.dbServer = dbServer;
        this.sessionFactory = sessionFactory;
        this.qaStep = qaStep;
        this.tableCreator = new TableCreator(dbServer.getEmissionsDatasource());
    }

    public void run() throws EmfException {
        String programArguments = qaStep.getProgramArguments();
        if (programArguments == null || programArguments.trim().length() == 0) {
            throw new EmfException("Please specify the sql query");
        }
        String tableName = tableName(qaStep);
        String query = query(dbServer, qaStep, tableName);
        try {
            dropTable(tableName);
            dbServer.getEmissionsDatasource().query().execute(query);
            success(qaStep, tableName);
        } catch (Exception e) {
            failure(qaStep);
            throw new EmfException("Check the query - " + query);
        }
    }

    private void dropTable(String tableName) throws EmfException {
        try {
            if (tableCreator.exists(tableName)) {
                tableCreator.drop(tableName);
            }
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    private void success(QAStep qaStep, String tableName) {
        updateQAStepResult(qaStep, "Success", tableName, new Date());
    }

    private void failure(QAStep qaStep) {
        updateQAStepResult(qaStep, "Failed", null, null);
    }

    private void updateQAStepResult(QAStep qaStep, String status, String tableName, Date date) {
        Session session = sessionFactory.getSession();
        try {
            QADAO qadao = new QADAO();
            QAStepResult result = qadao.qaStepResult(qaStep, session);
            if (result == null) {
                result = new QAStepResult(qaStep);
            }
            result.setTableCreationStatus(status);
            result.setTable(tableName);
            result.setTableCreationDate(date);
            qadao.updateQAStepResult(result, session);
        } finally {
            session.close();
        }
    }

    private String query(DbServer dbServer, QAStep qaStep, String tableName) throws EmfException {
        SQLQueryParser parser = new SQLQueryParser(qaStep, tableName, dbServer.getEmissionsDatasource().getName(),
                dataset(qaStep), version(qaStep));
        return parser.parse();
    }

    private EmfDataset dataset(QAStep qaStep) {
        DatasetDAO dao = new DatasetDAO();
        Session session = sessionFactory.getSession();
        try {
            return dao.getDataset(session, qaStep.getDatasetId());
        } finally {
            session.close();
        }
    }

    private Version version(QAStep qaStep) {
        Session session = sessionFactory.getSession();
        try {
            return new Versions().get(qaStep.getDatasetId(), qaStep.getVersion(), session);
        } finally {
            session.close();
        }
    }

    private String tableName(QAStep qaStep) {
        String result = "QA" + qaStep.getName() + "_DSID" + qaStep.getDatasetId() + "_V" + qaStep.getVersion();

        for (int i = 0; i < result.length(); i++) {
            if (!Character.isJavaLetterOrDigit(result.charAt(i))) {
                result = result.replace(result.charAt(i), '_');
            }
        }

        return result.trim().replaceAll(" ", "_");
    }

}
