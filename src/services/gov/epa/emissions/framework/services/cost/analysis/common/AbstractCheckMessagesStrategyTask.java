package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractCheckMessagesStrategyTask extends AbstractStrategyTask {

    private static final String STATUS_COLUMN_LABEL = "status";

    private static final String MESSAGE_COLUMN_LABEL = "message";

    public AbstractCheckMessagesStrategyTask(ControlStrategy controlStrategy, User user,
            DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory, StrategyLoader loader)
            throws EmfException {
        super(controlStrategy, user, dbServerFactory, sessionFactory, loader);
    }

    protected void checkMessagesForWarnings() throws EmfException {

        List<StatusMessage> messages = new ArrayList<StatusMessage>();

        ControlStrategyResult strategyMessagesResult = this.getLoader().getStrategyMessagesResult();
        InternalSource[] internalSources = strategyMessagesResult.getDetailedResultDataset().getInternalSources();
        if (internalSources.length > 0) {

            try {

                String tableName = internalSources[0].getTable();
                if (tableName != null) {

                    StringBuilder sb = new StringBuilder();
                    sb.append("select distinct ");
                    sb.append(STATUS_COLUMN_LABEL);
                    sb.append(", ");
                    sb.append(MESSAGE_COLUMN_LABEL);
                    sb.append(" from emissions.");
                    sb.append(tableName);
                    sb.append(";");

                    String sql = sb.toString();
                    System.out.println(sql);

                    ResultSet rs = datasource.query().executeQuery(sql);

                    String status = null;
                    String message = null;
                    while (rs.next()) {

                        status = rs.getString(STATUS_COLUMN_LABEL);
                        message = rs.getString(MESSAGE_COLUMN_LABEL);
                    }

                    if (status != null && message != null) {
                        messages.add(new StatusMessage(status, message, 2));
                    }

                }
            } catch (SQLException e) {
                throw new EmfException("Error occured while retreiving strategy messages for control strategy '"
                        + this.getControlStrategy().getName() + "':" + "\n" + e.getMessage());
            }
        }

        if (messages.size() < 10) {

            Collections.sort(messages);
            for (StatusMessage statusMessage : messages) {
                this.setStatus(statusMessage.createMessage());
            }
        } else {

            this.setStatus("Multiple warning messages were detected while running control strategy '"
                    + this.getControlStrategy().getName() + "'. See '" + StrategyResultType.strategyMessages
                    + "' output for details.");
        }
    }

    class StatusMessage implements Comparable<StatusMessage> {

        private String status;

        private String message;

        private int priority;

        public StatusMessage(String status, String message, int priority) {

            this.status = status;
            this.message = message;
            this.priority = priority;
        }

        public String getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public int getPriority() {
            return priority;
        }

        public String createMessage() {
            return this.status + ": " + this.message + " See '" + StrategyResultType.strategyMessages
                    + "' output for details.";
        }

        public int compareTo(StatusMessage o) {
            return Integer.valueOf(o.priority).compareTo(this.priority);
        }

    }
}