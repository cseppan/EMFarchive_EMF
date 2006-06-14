package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.lang.reflect.Constructor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StrategyFactory {
    private static Log log = LogFactory.getLog(StrategyFactory.class);

    private DbServer dbServer;

    public StrategyFactory(DbServer dbServer, SqlDataTypes sqlDataTypes) {
        this.dbServer = dbServer;
    }

    public Strategy create(EmfDataset dataset, ControlStrategy controlStrategy) throws EmfException {
        try {
            return doCreate(dataset, controlStrategy);
        } catch (Exception e) {
            log.error("Failed to create strategy. Cause: " + e.getMessage());
            throw new EmfException(e.getMessage());
        }
    }

    private Strategy doCreate(EmfDataset dataset, ControlStrategy controlStrategy) throws Exception {
        String strategyClassName = controlStrategy.getStrategyType().getStrategyClassName();
        Class strategyClass = Class.forName(strategyClassName);

        Class[] classParams = new Class[] { DbServer.class, ControlStrategy.class };
        Object[] params = new Object[] { dbServer, controlStrategy };
        
        Constructor strategyConstructor = strategyClass.getDeclaredConstructor(classParams);
        return (Strategy) strategyConstructor.newInstance(params);
    }
}
