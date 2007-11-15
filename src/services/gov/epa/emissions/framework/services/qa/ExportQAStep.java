package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.GCEnforcerTask;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class ExportQAStep {

    private QAStep step;
    
    private PooledExecutor threadPool;

    private DbServerFactory dbServerFactory;

    private HibernateSessionFactory sessionFactory;

    private User user;

    private static Log LOG = LogFactory.getLog(ExportQAStep.class);

    private boolean verboseStatusLogging = true;

    public ExportQAStep(QAStep step, DbServerFactory dbServerFactory, 
            User user, HibernateSessionFactory sessionFactory,
            PooledExecutor threadPool) {
        this.step = step;
        this.dbServerFactory = dbServerFactory;
        this.user = user;
        this.sessionFactory = sessionFactory;
        this.threadPool = threadPool;
    }

    public ExportQAStep(QAStep step, DbServerFactory dbServerFactory, 
            User user, HibernateSessionFactory sessionFactory,
            PooledExecutor threadPool, boolean verboseStatusLogging) {
        this(step, dbServerFactory,
            user, sessionFactory,
            threadPool);
        this.verboseStatusLogging = verboseStatusLogging;
    }

    public void export(String dirName) throws EmfException {
        ExportQAStepTask task = new ExportQAStepTask(dirName, step, 
                user, sessionFactory, 
                dbServerFactory, verboseStatusLogging);
        try {
            threadPool.execute(new GCEnforcerTask("Export QA Step : " + step.getProgram().getName(), task));
        } catch (InterruptedException e) {
            LOG.error("Error while exporting a qa step: " + step.getName(), e);
            throw new EmfException(e.getMessage());
        }
    }
}
