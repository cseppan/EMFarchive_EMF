package gov.epa.emissions.framework.services.cost.analysis.maxreduction;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.analysis.common.AbstractStrategyTask;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

public class MaxEmsRedStrategy extends AbstractStrategyTask {
    
    private StrategyLoader loader;
    
    public MaxEmsRedStrategy(ControlStrategy controlStrategy, User user, 
            DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory) throws EmfException {
        super(controlStrategy, user, 
                dbServerFactory, sessionFactory);
        this.loader = new StrategyLoader(user, dbServerFactory, 
                sessionFactory, controlStrategy);
    }

    public void run() throws EmfException {
        super.run(loader);
    }

    public void afterRun() throws EmfException {
        // NOTE Auto-generated method stub
        
        //now create the county summary result based on the results from the strategy run...
        generateStrategyCountySummaryResult(strategyResultList.toArray(new ControlStrategyResult[0]));
        
    }

    public void beforeRun() {
        // NOTE Auto-generated method stub
        
    }
}
