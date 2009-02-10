package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.lang.reflect.Constructor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RunQAProgramFactory {

    private QAStep qaStep;

    private DbServer dbServer;

    private Log log = LogFactory.getLog(RunQAProgramFactory.class);

    private HibernateSessionFactory sessionFactory;

    public RunQAProgramFactory(QAStep qaStep, DbServer dbServer, HibernateSessionFactory sessionFactory) {
        this.qaStep = qaStep;
        this.dbServer = dbServer;
        this.sessionFactory = sessionFactory;
    }

    public QAProgramRunner create() throws EmfException {
        try {
            return doCreate();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not create qa program runner ", e);
            throw new EmfException("Could not create qa program runner\n" + e.getMessage());
        }
    }

    private QAProgramRunner doCreate() throws Exception {
        if (qaStep.getProgram() == null)
            throw new Exception("A program should be specified to run a QA Step");
        String runClassName = qaStep.getProgram().getRunClassName();
        Class clazz = Class.forName(runClassName);
        Class[] classParams = { DbServer.class, HibernateSessionFactory.class, QAStep.class };
        Constructor declaredConstructor = clazz.getDeclaredConstructor(classParams);
        Object[] objectParams = { dbServer, sessionFactory, qaStep };
        return (QAProgramRunner) declaredConstructor.newInstance(objectParams);
    }

}
