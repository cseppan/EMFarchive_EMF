package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.framework.services.EmfDbServer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
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

    public void runQAStep(QAStep step) throws EmfException {
        updateQAStepBeforeRun(step);
        EmfDbServer dbServer = null;
        try {
            dbServer = new EmfDbServer();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
        RunQAStep runner = new RunQAStep(step,dbServer,sessionFactory,threadPool);
        runner.run();
    }

    private void updateQAStepBeforeRun(QAStep step) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.update(new QAStep[]{step},session);
        } catch (RuntimeException e) {
            LOG.error("could not update QA Step before run", e);
            throw new EmfException("could not update QA Step before run");
        } finally {
            session.close();
        }
        
    }
}
