package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.Services;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.cost.analysis.Strategy;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.FlushMode;
import org.hibernate.Session;

public class StrategyTask implements Runnable {

    private static Log log = LogFactory.getLog(StrategyTask.class);

    private User user;

    private Strategy strategy;

    private HibernateSessionFactory sessionFactory;

    private Services services;
    
    private ControlStrategyService csService;

    public StrategyTask(Strategy strategy, User user, Services services, HibernateSessionFactory sessionFactory,
            ControlStrategyService service) {
        this.user = user;
        this.services = services;
        this.sessionFactory = sessionFactory;
        this.strategy = strategy;
        this.csService = service;
    }

    public void run() {
        Session session = null;
        try {
            session = sessionFactory.getSession();
            session.setFlushMode(FlushMode.NEVER);
            
            prepare();
            strategy.run();
            csService.updateControlStrategyWithLock(strategy.getControlStrategy());
            complete();
        } catch (Exception e) {
            logError("Failed to run strategy : ", e);
            setStatus("Failed to run strategy: " + "Reason: " + e.getMessage());
        } finally {
            if (session != null) {
                session.flush();
                session.close();
            }
        }
    }

    private void prepare() {
        addStartStatus();
        strategy.getControlStrategy().setRunStatus("Started running strategy");
    }

    private void complete() {
        strategy.getControlStrategy().setRunStatus("Finished running strategy");
        strategy.getControlStrategy().setLastModifiedDate(new Date());

        addCompletedStatus();
    }

    private void addStartStatus() {
        setStatus("Started running control strategy: " + strategy.getControlStrategy().getName());
    }

    private void addCompletedStatus() {
        setStatus("Completed running control strategy: " + strategy.getControlStrategy().getName());
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
