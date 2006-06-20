package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.analysis.Strategy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

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
            throw new EmfException(e.getMessage());
        }
    }

    private Strategy doCreate(ControlStrategy controlStrategy)  {
        String strategyClassName = controlStrategy.getStrategyType().getStrategyClassName();
        Class strategyClass = null;
        try {
            strategyClass = Class.forName(strategyClassName);
        } catch (ClassNotFoundException e2) {
            // NOTE Auto-generated catch block
            e2.printStackTrace();
        }
       
        Class[] classParams = new Class[] { DbServer.class, CostService.class, ControlStrategy.class, Integer.class };
        Object[] params = new Object[] { dbServer, costService, controlStrategy, new Integer(batchSize) };
        Constructor strategyConstructor = null;
        try {
            strategyConstructor = strategyClass.getDeclaredConstructor(classParams);
        } catch (SecurityException e1) {
            // NOTE Auto-generated catch block
            e1.printStackTrace();
        } catch (NoSuchMethodException e1) {
            // NOTE Auto-generated catch block
            e1.printStackTrace();
        }
        
        try {
            return (Strategy) strategyConstructor.newInstance(params);
        } catch (IllegalArgumentException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
    }
    
}
