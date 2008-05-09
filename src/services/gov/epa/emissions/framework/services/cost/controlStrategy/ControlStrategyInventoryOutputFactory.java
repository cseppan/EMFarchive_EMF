package gov.epa.emissions.framework.services.cost.controlStrategy;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
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
        if (controlStrategyResult.getInputDataset().getDatasetType().getName().equals(DatasetType.orlMergedInventory))
            return new MergedControlStrategyInventoryOutput(user, controlStrategy, 
                    controlStrategyResult, sessionFactory, 
                    dbServerFactory);
        if (controlStrategyResult.getStrategyResultType().getName().equals(StrategyResultType.annotatedInventoryResult))
            return new AnnotatedControlStrategyInventoryOutput(user, controlStrategy, 
                    controlStrategyResult, sessionFactory, 
                    dbServerFactory);
        return new AbstractControlStrategyInventoryOutput(user, controlStrategy, 
                controlStrategyResult, sessionFactory, 
                dbServerFactory);
    }
}
