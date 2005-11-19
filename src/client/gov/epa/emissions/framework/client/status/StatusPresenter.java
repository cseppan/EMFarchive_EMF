package gov.epa.emissions.framework.client.status;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.TaskRunner;
import gov.epa.emissions.framework.services.Status;
import gov.epa.emissions.framework.services.StatusService;
import gov.epa.emissions.framework.services.User;

public class StatusPresenter {

    private StatusService model;

    private StatusView view;

    private User user;

    private StatusMonitor monitor;

    private TaskRunner runner;

    public StatusPresenter(User user, StatusService model, TaskRunner runner) {
        this.user = user;
        this.model = model;
        this.runner = runner;

        this.monitor = new StatusMonitor();
    }

    public void stop() {
        this.runner.stop();
    }

    public class StatusMonitor implements Runnable {
        public void run() {
            try {
                Status[] statuses = model.getMessages(user.getUsername());
                view.update(statuses);
            } catch (EmfException e) {
                view.notifyError(e.getMessage());
            }
        }
    }

    public void display(StatusView view) {
        this.view = view;
        view.display();

        runner.start(monitor);
    }

    public void close() {
        runner.stop();
    }
}
