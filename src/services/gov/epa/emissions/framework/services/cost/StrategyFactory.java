package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.analysis.Strategy;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.lang.reflect.Constructor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StrategyFactory {
    
    private static Log log = LogFactory.getLog(StrategyFactory.class);

    public StrategyFactory() {
        //
    }

    public Strategy create(ControlStrategy controlStrategy, User user, 
            HibernateSessionFactory sessionFactory, DbServerFactory dbServerFactory)
            throws EmfException {
        try {
            return doCreate(controlStrategy, user, 
                    sessionFactory, dbServerFactory);
        } catch (Exception e) {
            log.error("Failed to create strategy. Cause: " + e.getMessage());
            throw new EmfException("Failed to create strategy." + e.getMessage());
        }
    }

    private Strategy doCreate(ControlStrategy controlStrategy, User user, 
            HibernateSessionFactory sessionFactory, DbServerFactory dbServerFactory)
            throws Exception {
        String strategyClassName = controlStrategy.getStrategyType().getStrategyClassName();
        Class strategyClass = Class.forName(strategyClassName);
        Class[] classParams = new Class[] { ControlStrategy.class, User.class, 
                DbServerFactory.class, HibernateSessionFactory.class };
        Object[] params = new Object[] { controlStrategy, user, 
                dbServerFactory, sessionFactory };
        Constructor strategyConstructor = strategyClass.getDeclaredConstructor(classParams);

        return (Strategy) strategyConstructor.newInstance(params);
    }
}