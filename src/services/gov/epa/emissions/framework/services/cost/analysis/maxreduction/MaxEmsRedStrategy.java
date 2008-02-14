package gov.epa.emissions.framework.services.cost.analysis.maxreduction;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.analysis.common.AbstractStrategyTask;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

public class MaxEmsRedStrategy extends AbstractStrategyTask {
    
    private StrategyLoader loader;
    
    public MaxEmsRedStrategy(ControlStrategy controlStrategy, User user, 
            DbServerFactory dbServerFactory, Integer batchSize,
            HibernateSessionFactory sessionFactory) throws EmfException {
        super(controlStrategy, user, 
                dbServerFactory, sessionFactory);
        this.loader = new StrategyLoader(user, dbServerFactory, 
                sessionFactory, controlStrategy, 
                batchSize);
    }

    public MaxEmsRedStrategy(ControlStrategy controlStrategy, User user, 
            DbServerFactory dbServerFactory, Integer batchSize,
            HibernateSessionFactory sessionFactory, Boolean useSQLApproach) throws EmfException {
        super(controlStrategy, user, 
                dbServerFactory, sessionFactory);
        this.loader = new StrategyLoader(user, dbServerFactory, 
                sessionFactory, controlStrategy, 
                batchSize, useSQLApproach);
    }

    public void run() throws EmfException {
        super.run(loader);
    }
}
