package gov.epa.emissions.framework.services.cost.controlStrategy;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.StrategyType;
import gov.epa.emissions.framework.services.cost.analysis.leastcost.LeastCostControlStrategyInventoryOutput;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

public class ControlStrategyInventoryOutputFactory {

    private ControlStrategy controlStrategy;

    private User user;

    private HibernateSessionFactory sessionFactory;

    private DbServerFactory dbServerFactory;

    public ControlStrategyInventoryOutputFactory(User user, ControlStrategy controlStrategy,
            HibernateSessionFactory sessionFactory, DbServerFactory dbServerFactory) throws Exception {
        this.controlStrategy = controlStrategy;
        this.user = user;
        this.sessionFactory = sessionFactory;
        this.dbServerFactory = dbServerFactory;
    }

    public ControlStrategyInventoryOutput get(ControlStrategyResult controlStrategyResult) throws Exception {
        if (controlStrategy.getStrategyType().getName().equals(StrategyType.leastCost))
            return new LeastCostControlStrategyInventoryOutput(user, controlStrategy, 
                    controlStrategyResult, sessionFactory, 
                    dbServerFactory);
        return new AbstractControlStrategyInventoryOutput(user, controlStrategy, 
                controlStrategyResult, sessionFactory, 
                dbServerFactory);
    }
}