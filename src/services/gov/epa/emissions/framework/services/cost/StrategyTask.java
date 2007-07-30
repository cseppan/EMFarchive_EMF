package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.Services;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.cost.analysis.Strategy;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StrategyTask implements Runnable {

    private static Log log = LogFactory.getLog(StrategyTask.class);

    private User user;

    private Strategy strategy;

    private Services services;

    private ControlStrategyService csService;

    public StrategyTask(Strategy strategy, User user, Services services, ControlStrategyService service) {
        this.user = user;
        this.services = services;
        this.strategy = strategy;
        this.csService = service;
    }

    public void run() {
        String completeStatus = "";
        try {
            prepare();
            strategy.run();
            completeStatus = "Finished";
            addCompletedStatus();
        } catch (Exception e) {
            completeStatus = "Failed";
            logError("Failed to run strategy : ", e);
            setStatus("Failed to run strategy: " + "Reason: " + e.getMessage());
        } finally {
            complete(completeStatus);
//            closeConnection();
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
        strategy.getControlStrategy().setRunStatus("Running");
        csService.updateControlStrategyWithLock(strategy.getControlStrategy());
        addStartStatus();
    }

    private void complete(String completeStatus) {
        strategy.getControlStrategy().setRunStatus(completeStatus);
        strategy.getControlStrategy().setLastModifiedDate(new Date());
        updateStrategy();
    }

    private void updateStrategy() {
        try {
            csService.updateControlStrategy(strategy.getControlStrategy());
        } catch (EmfException e) {
            logError("Failed to update the strategy : ", e);
            setStatus("Failed to update strategy: " + "Reason: " + e.getMessage());
        }

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
