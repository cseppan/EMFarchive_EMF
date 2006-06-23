package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.analysis.Strategy;

import java.lang.reflect.Constructor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StrategyFactory {
    private static Log log = LogFactory.getLog(StrategyFactory.class);

    private DbServer dbServer;

    private CostService costService;

    private int batchSize;

    public StrategyFactory(DbServer dbServer, CostService costService, int batchSize) {
        this.dbServer = dbServer;
        this.costService = costService;
        this.batchSize = batchSize;
    }

    public Strategy create(ControlStrategy controlStrategy) throws EmfException {
        try {
            return doCreate(controlStrategy);
        } catch (Exception e) {
            log.error("Failed to create strategy. Cause: " + e.getMessage());
            throw new EmfException("Failed to create strategy." + e.getMessage());
        }
    }

    private Strategy doCreate(ControlStrategy controlStrategy) throws Exception {
        String strategyClassName = controlStrategy.getStrategyType().getStrategyClassName();
        Class strategyClass  = Class.forName(strategyClassName);

        Class[] classParams = new Class[] { DbServer.class, CostService.class, ControlStrategy.class, Integer.class };
        Object[] params = new Object[] { dbServer, costService, controlStrategy, new Integer(batchSize) };
        Constructor strategyConstructor = strategyClass.getDeclaredConstructor(classParams);
        
        return (Strategy) strategyConstructor.newInstance(params);
    }

}
