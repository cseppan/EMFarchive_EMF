package gov.epa.emissions.framework.services.cost.analysis.applyMeasuresInSeries;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.analysis.common.AbstractStrategyTask;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

public class StrategyTask extends AbstractStrategyTask {

    private StrategyLoader loader;
    
    public StrategyTask(ControlStrategy controlStrategy, User user, 
            DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory) throws EmfException {
        super(controlStrategy, user, 
                dbServerFactory, sessionFactory);
        this.loader = new StrategyLoader(user, dbServerFactory, 
                sessionFactory, controlStrategy);
    }

    public void run() throws EmfException {
        super.run(loader);
    }

    public void afterRun() {
        // NOTE Auto-generated method stub
        
    }

    public void beforeRun() {
        // NOTE Auto-generated method stub
        
    }
}