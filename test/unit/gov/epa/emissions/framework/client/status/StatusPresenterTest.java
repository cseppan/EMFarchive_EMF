package gov.epa.emissions.framework.client.status;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.security.UserException;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.TaskRunner;
import gov.epa.emissions.framework.client.status.StatusPresenter;
import gov.epa.emissions.framework.client.status.StatusView;
import gov.epa.emissions.framework.client.status.StatusPresenter.StatusMonitor;
import gov.epa.emissions.framework.services.Status;
import gov.epa.emissions.framework.services.StatusService;

import java.util.Date;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.constraint.IsInstanceOf;

public class StatusPresenterTest extends MockObjectTestCase {

    private Mock view;

    private Mock service;

    private User user;

    private TaskRunner runner;

    protected void setUp() throws UserException {
        user = new User();
        user.setUsername("user");

        service = mock(StatusService.class);
        view = mock(StatusView.class);        

        runner = new TaskRunner() {
            public void start(Runnable runnable) {
                runnable.run();
            }

            public void stop() {// No Op
            }
        };
    }

    public void testShouldShowViewAndStartPollOnDisplay() throws Exception {
        Status status = new Status(user.getUsername(), "type", "message", new Date());
        Status[] messages = new Status[] { status };

        service.expects(atLeastOnce()).method("getAll").with(eq(user.getUsername())).will(returnValue(messages));
        view.expects(atLeastOnce()).method("update").with(same(messages));
        view.expects(once()).method("display").withNoArguments();

        StatusPresenter presenter = new StatusPresenter(user, (StatusService) service.proxy(), runner);
        presenter.display((StatusView) view.proxy());

        presenter.stop();
    }

    public void testShouldNotifyViewOnFailedPoll() throws Exception {
        service.expects(atLeastOnce()).method("getAll").with(eq(user.getUsername())).will(
                throwException(new EmfException("poll failure")));

        Mock view = mock(StatusView.class);
        view.expects(once()).method("notifyError").with(eq("poll failure"));
        view.expects(once()).method("display").withNoArguments();

        StatusPresenter presenter = new StatusPresenter(user, (StatusService) service.proxy(), runner);
        presenter.display((StatusView) view.proxy());

        presenter.stop();
    }
    
    public void testShouldStopRunnerOnClose() throws Exception {
        view.expects(once()).method("display").withNoArguments();

        Mock runner = mock(TaskRunner.class);
        runner.expects(once()).method("start").with(new IsInstanceOf(StatusMonitor.class));
        runner.expects(once()).method("stop").withNoArguments();
        
        StatusPresenter presenter = new StatusPresenter(user, (StatusService) service.proxy(), (TaskRunner) runner.proxy());        
        presenter.display((StatusView) view.proxy());

        presenter.close();
    }

}
