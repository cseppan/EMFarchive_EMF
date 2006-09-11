package gov.epa.emissions.framework.services.qa;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.framework.services.EmfDbServer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.QAStep;

public class RunQAProgramFactory {

    private QAStep qaStep;

    private EmfDbServer dbServer;
    
    private Log log = LogFactory.getLog(RunQAProgramFactory.class);

    public RunQAProgramFactory(QAStep qaStep, EmfDbServer dbServer) {
        this.qaStep = qaStep;
        this.dbServer = dbServer;
    }

    public QAProgramRunner create() throws EmfException  {
        try {
            return doCreate();
        } catch (Exception e) {
            log.error("Could not create qa program runner ",e);
            throw new EmfException("Could not create qa program runner\n"+e.getMessage());
        }
    }

    private QAProgramRunner doCreate() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        String runClassName = qaStep.getProgram().getRunClassName();
        Class clazz = Class.forName(runClassName);
        Class[] classParams = { DbServer.class, QAStep.class };
        Constructor declaredConstructor = clazz.getDeclaredConstructor(classParams);
        Object[] objectParams = { dbServer, qaStep };
        return (QAProgramRunner) declaredConstructor.newInstance(objectParams);
    }

}
