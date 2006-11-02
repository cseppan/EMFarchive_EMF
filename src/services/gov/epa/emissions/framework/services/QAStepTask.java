package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.services.qa.QADAO;
import gov.epa.emissions.framework.services.qa.RunQAStep;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class QAStepTask {

    private static Log LOG = LogFactory.getLog(QAStepTask.class);

    private HibernateSessionFactory sessionFactory;

    private EmfDataset dataset;

    private PooledExecutor threadPool;

    private User user;

    private int version;

    public QAStepTask(HibernateSessionFactory sessionFactory, EmfDataset dataset, int version, User user) {
        this.sessionFactory = sessionFactory;
        this.dataset = dataset;
        this.threadPool = createThreadPool();
        this.user = user;
        this.version = version;
    }

    private PooledExecutor createThreadPool() {
        PooledExecutor threadPool = new PooledExecutor(20);
        threadPool.setMinimumPoolSize(1);
        threadPool.setKeepAliveTime(1000 * 60 * 3);// terminate after 3 (unused) minutes

        return threadPool;
    }

    private QAStepTemplate[] getSummaryTemplates(String[] summaryQAStepNames) {
        List summaryTemplates = new ArrayList();
        QAStepTemplate[] templates = dataset.getDatasetType().getQaStepTemplates();
        
        for (int i = 0; i < templates.length; i++) {
            String name = templates[i].getName().trim();
            for (int j = 0; j < summaryQAStepNames.length; j++)
                if (name.equalsIgnoreCase(summaryQAStepNames[j])) {
                    summaryTemplates.add(templates[i]);
                }
        }

        return (QAStepTemplate[]) summaryTemplates.toArray(new QAStepTemplate[0]);
    }

    public void checkAndRunSummaryQASteps(String[] qaStepNames) throws EmfException {
        QAStepTemplate[] summaryTemplates = getSummaryTemplates(qaStepNames);
        
        if (summaryTemplates.length < qaStepNames.length)
            throw new EmfException("Summary QAStepTemplate doesn't exist in dataset type: "
                    + dataset.getDatasetTypeName());

        QAStep[] summarySteps = addQASteps(summaryTemplates, dataset, version);

        try {
            for (int i = 0; i < summarySteps.length; i++) {
                QAStepResult qaResult = getQAStepResult(summarySteps[i]);
                if (qaResult == null || (qaResult != null && !qaResult.isCurrentTable()))
                    runQAStep(summarySteps[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException("Cann't run summary QASteps: " + e.getMessage());
        }
    }

    private void runQAStep(QAStep step) throws EmfException {
        EmfDbServer dbServer = dbServer();
        RunQAStep runner = new RunQAStep(step, user, dbServer, sessionFactory, threadPool);
        runner.run();
    }

    private QAStep[] addQASteps(QAStepTemplate[] templates, EmfDataset dataset, int version) throws EmfException {
        QAStep[] steps = new QAStep[templates.length];

        for (int i = 0; i < templates.length; i++) {
            QAStep step = new QAStep(templates[i], version);
            step.setDatasetId(dataset.getId());
            steps[i] = step;
        }

        updateSteps(getNonExistingSteps(steps));

        return steps;
    }

    private void updateSteps(QAStep[] steps) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            QADAO dao = new QADAO();
            dao.updateQAStepsIds(steps, session);
            dao.update(steps, session);
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new EmfException("Could not update QA Steps");
        }
    }

    private QAStep[] getNonExistingSteps(QAStep[] steps) throws EmfException {
        List stepsList = new ArrayList();

        Session session = sessionFactory.getSession();
        try {
            QADAO dao = new QADAO();
            for (int i = 0; i < steps.length; i++) {
                if (!dao.exists(steps[i], session))
                    stepsList.add(steps[i]);
            }
        } catch (RuntimeException e) {
            throw new EmfException("Could not check QA Steps");
        }

        return (QAStep[]) stepsList.toArray(new QAStep[0]);
    }

    private EmfDbServer dbServer() throws EmfException {
        EmfDbServer dbServer = null;
        try {
            dbServer = new EmfDbServer();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
        return dbServer;
    }

    private QAStepResult getQAStepResult(QAStep step) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            QADAO dao = new QADAO();
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
    
    public String[] getDefaultSummaryQANames() {
        Session session = sessionFactory.getSession();
        List summaryQAList = new ArrayList();
        
        try {
            EmfProperty property = new EmfPropertiesDAO().getProperty("defaultQASummaries", session);
            String qaString = property.getValue().trim();
            StringTokenizer st = new StringTokenizer(qaString, "|");
            
            while (st.hasMoreTokens())
                summaryQAList.add(st.nextToken().trim());
            
            return (String[])summaryQAList.toArray(new String[0]);
        } finally {
            session.close();
        }
    }
    
}
