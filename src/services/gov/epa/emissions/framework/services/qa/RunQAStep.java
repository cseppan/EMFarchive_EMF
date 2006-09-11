package gov.epa.emissions.framework.services.qa;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;
import gov.epa.emissions.framework.services.EmfDbServer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.GCEnforcerTask;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

public class RunQAStep {

    private QAStep qaStep;

    private EmfDbServer dbServer;

    private HibernateSessionFactory sessionFactory;

    private PooledExecutor threadPool;

    private static Log log = LogFactory.getLog(RunQAStep.class);

    public RunQAStep(QAStep step, EmfDbServer dbServer, HibernateSessionFactory sessionFactory,
            PooledExecutor threadPool) {
        this.qaStep = step;
        this.dbServer = dbServer;
        this.sessionFactory = sessionFactory;
        this.threadPool = threadPool;
    }

    public void run() throws EmfException {
        RunQAProgramFactory factory = new RunQAProgramFactory(qaStep,dbServer);
        QAProgramRunner runQAProgram = factory.create();
        RunQAStepTask task = new RunQAStepTask(qaStep, runQAProgram, dbServer, sessionFactory);
        try {
            threadPool.execute(new GCEnforcerTask("Run QA Program : " + qaStep.getProgram().getName(), task));
        } catch (InterruptedException e) {
            log.error("Error running qa step: " + qaStep.getName(), e);
            throw new EmfException(e.getMessage());
        }
    }

}
