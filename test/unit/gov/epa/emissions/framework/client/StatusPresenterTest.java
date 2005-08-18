package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.TaskRunner;
import gov.epa.emissions.framework.services.Status;
import gov.epa.emissions.framework.services.StatusServices;
import gov.epa.emissions.framework.services.User;

import java.util.Date;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class StatusPresenterTest extends MockObjectTestCase {

    public void testShouldUpdateViewOnSuccessfulPoll() throws Exception {
        User user = new User();
        user.setUserName("user");

        Status status = new Status(user.getUserName(), "type", "message", new Date());
        Status[] messages = new Status[] { status };
        Mock service = mock(StatusServices.class);
        service.expects(atLeastOnce()).method("getMessages").with(eq(user.getUserName())).will(returnValue(messages));

        Mock view = mock(StatusView.class);
        view.expects(atLeastOnce()).method("update").with(same(messages));

        StatusPresenter presenter = new StatusPresenter(user, (StatusServices) service.proxy(), (StatusView) view
                .proxy());

        TaskRunner runner = new TaskRunner() {
            public void start(Runnable runnable) {
                runnable.run();
            }

            public void stop() {
            }
        };
        presenter.start(runner);
        presenter.stop();
    }

    public void testShouldNotifyViewOnFailedPoll() throws Exception {
        User user = new User();
        user.setUserName("user");

        Mock service = mock(StatusServices.class);
        service.expects(atLeastOnce()).method("getMessages").with(eq(user.getUserName())).will(
                throwException(new EmfException("poll failure")));

        Mock view = mock(StatusView.class);
        view.expects(once()).method("notifyError").with(eq("poll failure"));

        StatusPresenter presenter = new StatusPresenter(user, (StatusServices) service.proxy(), (StatusView) view
                .proxy());

        TaskRunner runner = new TaskRunner() {
            public void start(Runnable runnable) {
                runnable.run();
            }

            public void stop() {
            }
        };
        presenter.start(runner);
        presenter.stop();
    }
}
