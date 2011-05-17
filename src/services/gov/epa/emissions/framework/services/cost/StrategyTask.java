package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.EmfProperty;
import gov.epa.emissions.framework.services.Services;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.cost.analysis.Strategy;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

public class StrategyTask implements Runnable {

    private static Log log = LogFactory.getLog(StrategyTask.class);

    private User user;

    private Strategy strategy;

    private Services services;

    private ControlStrategyService csService;

    private HibernateSessionFactory sessionFactory;

    public StrategyTask(Strategy strategy, User user, 
            Services services, ControlStrategyService service,
            HibernateSessionFactory sessionFactory) {
        this.user = user;
        this.services = services;
        this.strategy = strategy;
        this.csService = service;
        this.sessionFactory = sessionFactory;
    }

    public void run() {
        String completeStatus = "";
//         //set strategy run status to waiting, this will make sure it is run in the make-shift queue
//        try {
//            setRunStatus("Waiting");
//        } catch (EmfException e1) {
//            // NOTE Auto-generated catch block
//            e1.printStackTrace();
//        }
        Long poolSize = strategyPoolSize();
        Long runningCount = getControlStrategyRunningCount();
        
        //make sure we can add an work item to the queue
        //check the pool size and compare to the number of strategies currently running
        if (runningCount < poolSize) {
            try {
                prepare();
                log.debug("Started to run strategy");
                strategy.run();
                log.debug("Finished to run strategy");
                completeStatus = "Finished";
                addCompletedStatus();
            } catch (EmfException e) {
                completeStatus = "Failed";
                logError("Failed to run strategy : ", e);
                setStatus("Failed to run strategy: " + "Reason: " + e.getMessage());
            } finally {
//                    closeConnection();
                
//                strategy.getControlStrategy().setRunStatus(completeStatus);
//                strategy.getControlStrategy().setCompletionDate(new Date());

                //check to see if there is another strategy to run...
                List<ControlStrategy> waitingStrategies;
                try {
                    setRunStatusAndCompletionDate(completeStatus, new Date());
                    waitingStrategies = csService.getControlStrategiesByRunStatus("Waiting");
                    if (waitingStrategies.size() > 0) {
                        runningCount = getControlStrategyRunningCount();
                        if (runningCount < poolSize) {
                            for (ControlStrategy controlStrategy : waitingStrategies.toArray(new ControlStrategy[0])) {
                                csService.runStrategy(user, controlStrategy.getId());
                                runningCount++;
                            }
                        }
                    }
                } catch (EmfException e) {
                    // NOTE Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

//    private void closeConnection() {
//        try {
//            strategy.close();
//        } catch (EmfException e) {
//            logError("Failed to close connection : ", e);
//            setStatus("Failed to close connection " + "Reason: " + e.getMessage());
//        }
//    }
//
    private void prepare() throws EmfException {
        ControlStrategy controlStrategy = strategy.getControlStrategy();
        controlStrategy = csService.obtainLocked(controlStrategy.getCreator(), controlStrategy.getId());
        controlStrategy.setStartDate(new Date());
        controlStrategy.setRunStatus("Running");
        csService.updateControlStrategyWithLock(controlStrategy);
        addStartStatus();
    }

    private void setRunStatusAndCompletionDate(String completeStatus, Date completionDate) throws EmfException {
        strategy.getControlStrategy().setRunStatus(completeStatus);
        strategy.getControlStrategy().setLastModifiedDate(new Date());
        strategy.getControlStrategy().setCompletionDate(completionDate);
//        updateStrategy();
        csService.setControlStrategyRunStatusAndCompletionDate(strategy.getControlStrategy().getId(), completeStatus, completionDate);
    }

//    private void updateStrategy() {
//        try {
//            csService.updateControlStrategy(strategy.getControlStrategy());
//        } catch (EmfException e) {
//            logError("Failed to update the strategy : ", e);
//            setStatus("Failed to update strategy: " + "Reason: " + e.getMessage());
//        }
//
//    }

    private Long strategyPoolSize() {
        Session session = sessionFactory.getSession();
        try {
            EmfProperty property = new EmfPropertiesDAO().getProperty("strategy-pool-size", session);
            return Long.parseLong(property.getValue());
        } finally {
            session.close();
        }
    }
    private Long getControlStrategyRunningCount() {
        Long count = 0L;
        try {
            count = csService.getControlStrategyRunningCount();
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        return count;
    }
    
    private void addStartStatus() {
        setStatus("Started running control strategy: " + strategy.getControlStrategy().getName());
    }

    private void addCompletedStatus() {
        setStatus("Completed running control strategy: " + strategy.getControlStrategy().getName() + ".");
    }

    private void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("Strategy");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        services.getStatus().add(endStatus);
    }

    private void logError(String messge, Exception e) {
        log.error(messge, e);
    }

}
