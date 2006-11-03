package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.Date;

import org.hibernate.Session;

public class RunQAStepTask {

    private QAStep[] qasteps;

    private User user;

    private StatusDAO statusDao;

    private HibernateSessionFactory sessionFactory;

    private DbServer dbServer;

    public RunQAStepTask(QAStep[] qaStep, User user, DbServer dbServer, HibernateSessionFactory sessionFactory) {
        this.qasteps = qaStep;
        this.user = user;
        this.dbServer = dbServer;
        this.sessionFactory = sessionFactory;
        this.statusDao = new StatusDAO(sessionFactory);
    }

    public void run() throws EmfException {
        QAStep qaStep = null;
        try {
            for (int i = 0; i < qasteps.length; i++) {
                qaStep = qasteps[i];
                runSteps(qaStep);
            }
        } catch (EmfException e) {
            setStatus("Failed to run QA step " + qaStep.getName() + suffix(qaStep) + ". " + e.getMessage());
            throw new EmfException("Failed to run QA step : " + qaStep.getName() + suffix(qaStep));
        }
    }

    private void runSteps(QAStep qaStep) throws EmfException {
        String suffix = suffix(qaStep);
        prepare(suffix, qaStep);
        QAProgramRunner runQAProgram = qaProgramRunner(qaStep);
        runQAProgram.run();
        complete(suffix, qaStep);
    }

    private QAProgramRunner qaProgramRunner(QAStep step) throws EmfException {
        RunQAProgramFactory factory = new RunQAProgramFactory(step, dbServer, sessionFactory);
        try {
            return factory.create();
        } catch (EmfException e) {
            throw new EmfException("Could not create the program runner");
        }
    }

    private void prepare(String suffixMsg, QAStep qastep) {
        setStatus("Started running QA step '" + qastep.getName() + suffixMsg);
    }

    private void complete(String suffixMsg, QAStep qastep) {
        setStatus("Completed running QA step '" + qastep.getName() + suffixMsg);
    }

    private void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("RunQAStep");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusDao.add(endStatus);

    }

    private String suffix(QAStep qastep) {
        return "' for Version '" + versionName(qastep) + "' of Dataset '" + datasetName(qastep) + "'";
    }

    private String versionName(QAStep qastep) {
        Session session = sessionFactory.getSession();
        try {
            return new Versions().get(qastep.getDatasetId(), qastep.getVersion(), session).getName();
        } finally {
            session.close();
        }
    }

    private String datasetName(QAStep qastep) {
        Session session = sessionFactory.getSession();
        try {
            DatasetDAO dao = new DatasetDAO();
            return dao.getDataset(session, qastep.getDatasetId()).getName();
        } finally {
            session.close();
        }
    }

}
