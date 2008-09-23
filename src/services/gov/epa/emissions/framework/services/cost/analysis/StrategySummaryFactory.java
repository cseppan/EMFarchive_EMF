package gov.epa.emissions.framework.services.cost.analysis;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StrategySummaryFactory {
    
    private static Log log = LogFactory.getLog(StrategySummaryFactory.class);

    public StrategySummaryFactory() {
        //
    }

    public IStrategySummaryTask create(ControlStrategy controlStrategy, User user, 
            StrategyResultType strategyResultType, HibernateSessionFactory sessionFactory, DbServerFactory dbServerFactory)
            throws EmfException {
        try {
//            if (strategyResultType.getName().equals(StrategyResultType.strategyCountySummary))
                    return new StrategyCountySummaryTask(controlStrategy, user, 
                            dbServerFactory, sessionFactory);
//            return null;
        } catch (Exception e) {
            log.error("Failed to create strategy. Cause: " + e.getMessage());
            throw new EmfException("Failed to create strategy." + e.getMessage());
        }
    }
}