package gov.epa.emissions.framework.client.status;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.TaskRunner;
import gov.epa.emissions.framework.services.Status;
import gov.epa.emissions.framework.services.StatusServices;
import gov.epa.emissions.framework.services.User;

public class StatusPresenter {

    private StatusServices model;

    private StatusView view;

    private User user;

    private StatusMonitor monitor;

    private TaskRunner runner;

    public StatusPresenter(User user, StatusServices model, StatusView view) {
        this.user = user;
        this.model = model;
        this.view = view;

        this.monitor = new StatusMonitor();
    }

    public void start(TaskRunner runner) {
        runner.start(monitor);
        this.runner = runner;
    }

    public void stop() {
        this.runner.stop();
    }

    public class StatusMonitor implements Runnable {
        public void run() {
            try {
                Status[] statuses = model.getMessages(user.getUserName());
                view.update(statuses);
            } catch (EmfException e) {
                view.notifyError(e.getMessage());
            }
        }
    }
}
