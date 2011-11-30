package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.data.ProjectionShapeFile;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.GCEnforcerTask;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class ExportShapeFileQAStep {

    private QAStep step;
    
    private PooledExecutor threadPool;

    private DbServerFactory dbServerFactory;

    private HibernateSessionFactory sessionFactory;

    private User user;

    private static Log LOG = LogFactory.getLog(ExportShapeFileQAStep.class);

    private boolean verboseStatusLogging = true;

    private Pollutant pollutant;
//    public ExportShapeFileQAStep(QAStep step, DbServerFactory dbServerFactory, 
//            User user, HibernateSessionFactory sessionFactory,
//            PooledExecutor threadPool) {
//        this.step = step;
//        this.dbServerFactory = dbServerFactory;
//        this.user = user;
//        this.sessionFactory = sessionFactory;
//        this.threadPool = threadPool;
//    }

    public ExportShapeFileQAStep(QAStep step, DbServerFactory dbServerFactory, 
            User user, HibernateSessionFactory sessionFactory,
            PooledExecutor threadPool, boolean verboseStatusLogging,
            Pollutant pollutant) {
//        this(step, dbServerFactory,
//            user, sessionFactory,
//            threadPool);
        this.step = step;
        this.dbServerFactory = dbServerFactory;
        this.user = user;
        this.sessionFactory = sessionFactory;
        this.threadPool = threadPool;
        this.verboseStatusLogging = verboseStatusLogging;
        this.pollutant = pollutant;
    }

    public void export(String dirName, String fileName, ProjectionShapeFile projectionShapeFile, boolean overide) throws EmfException {
        ExportShapeFileQAStepTask task = new ExportShapeFileQAStepTask(dirName, fileName, 
                overide, step, 
                user, sessionFactory,
                dbServerFactory, projectionShapeFile, verboseStatusLogging, pollutant);
        try {
            threadPool.execute(new GCEnforcerTask("Export QA Step : " + step.getProgram().getName(), task));
        } catch (InterruptedException e) {
            LOG.error("Error while exporting a qa step: " + step.getName(), e);
            throw new EmfException(e.getMessage());
        }
    }
}
