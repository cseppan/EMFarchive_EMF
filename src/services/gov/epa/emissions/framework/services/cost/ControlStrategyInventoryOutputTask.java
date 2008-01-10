package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyInventoryOutput;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ControlStrategyInventoryOutputTask implements Runnable {

    private static final Log LOG = LogFactory.getLog(ControlStrategyInventoryOutputTask.class);

    private User user;

    private ControlStrategy controlStrategy;

    private HibernateSessionFactory sessionFactory;

    private DbServerFactory dbServerFactory;
    
    private ControlStrategyResult[] controlStrategyResults;
    
    public ControlStrategyInventoryOutputTask(User user, ControlStrategy controlStrategy,
            ControlStrategyResult[] controlStrategyResults, HibernateSessionFactory sessionFactory, 
            DbServerFactory dbServerFactory) {
        this.user = user;
        this.controlStrategy = controlStrategy;
        this.controlStrategyResults = controlStrategyResults;
        this.sessionFactory = sessionFactory;
        this.dbServerFactory = dbServerFactory;
    }

    public void run() {
        try {
            for (int i = 0; i < controlStrategyResults.length; i++) {
                if (controlStrategyResults[i].getStrategyResultType().getName().equals("Detailed Strategy Result")) {
                    ControlStrategyInventoryOutput output = new ControlStrategyInventoryOutput(user, controlStrategy,
                            controlStrategyResults[i], sessionFactory, 
                            dbServerFactory);
                    output.create();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("Could not create inventory output. " + e.getMessage());
        }
    }

    private void close(DbServer dbServer) {
        try {
            if (dbServer != null)
                dbServer.disconnect();
        } catch (Exception e) {
            LOG.error("Could not close database connection." + e.getMessage());
        }
    }

    public boolean shouldProceed() throws EmfException {
        DbServer dbServer = dbServerFactory.getDbServer();
        try {
            for (int i = 0; i < controlStrategyResults.length; i++) {
                if (controlStrategyResults[i].getStrategyResultType().getName().equals("Detailed Strategy Result")) {
                    Dataset detailedResultDataset = controlStrategyResults[i].getDetailedResultDataset();
                    if (detailedResultDataset == null)
                        throw new EmfException("You should run the control strategy first before creating the inventory, input inventory - " + controlStrategyResults[i].getInputDataset().getName());
                    String detailResultTableName = detailedResultDataset.getInternalSources()[0].getTable();
                    int totalRows = dbServer.getEmissionsDatasource().tableDefinition().totalRows(detailResultTableName);
                    if (totalRows == 0) {
                        throw new EmfException(
                                "Control Strategy Result does not have any data in the table. Control inventory is not created, input inventory - " + controlStrategyResults[i].getInputDataset().getName());
                    }
                }
            }
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } finally {
            close(dbServer);
        }
        return true;
    }
}