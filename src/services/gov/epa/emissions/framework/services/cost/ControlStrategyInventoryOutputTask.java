package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyInventoryOutput;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

public class ControlStrategyInventoryOutputTask implements Runnable {

    private static final Log LOG = LogFactory.getLog(ControlStrategyInventoryOutputTask.class);

    private User user;

    private ControlStrategy controlStrategy;

    private HibernateSessionFactory sessionFactory;

    private DbServer dbServer;

    public ControlStrategyInventoryOutputTask(User user, ControlStrategy controlStrategy,
            HibernateSessionFactory sessionFactory, DbServer dbServer) {
        this.user = user;
        this.controlStrategy = controlStrategy;
        this.sessionFactory = sessionFactory;
        this.dbServer = dbServer;
    }

    public void run() {
        try {
            ControlStrategyInventoryOutput output = new ControlStrategyInventoryOutput(user, controlStrategy,
                    sessionFactory, dbServer);
            output.create();
        } catch (Exception e) {
            LOG.error("Could not create inventory output. " + e.getMessage());
        } finally {
            close(dbServer);
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
        Session session = sessionFactory.getSession();
        try {
            ControlStrategyResult result = new ControlStrategyDAO().controlStrategyResult(controlStrategy, session);
            Dataset detailedResultDataset = result.getDetailedResultDataset();
            if (detailedResultDataset == null)
                throw new EmfException("You should run the control strategy first before creating the inventory");
            String detailResultTableName = detailedResultDataset.getInternalSources()[0].getTable();
            int totalRows = dbServer.getEmissionsDatasource().tableDefinition().totalRows(detailResultTableName);
            if (totalRows == 0) {
                throw new EmfException(
                        "Control Strategy Result does not have any data in the table. Control inventory is not created");
            }
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }
        return true;
    }
}
