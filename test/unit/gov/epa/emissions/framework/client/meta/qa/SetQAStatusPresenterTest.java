package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.data.QAStep;

import org.jmock.Mock;
import org.jmock.core.Constraint;

public class SetQAStatusPresenterTest extends EmfMockObjectTestCase {

    public void testShouldRefreshTabViewAndCloseOnSave() {
        Mock tabView = mock(EditableQATabView.class);
        expects(tabView, "refresh");

        Mock view = mock(SetQAStatusView.class);
        expects(view, "close");

        SetQAStatusPresenter presenter = new SetQAStatusPresenter((SetQAStatusView) view.proxy(), null,
                (EditableQATabView) tabView.proxy(), null);
        presenter.doSave();
    }

    public void testShouldCloseViewOnClose() {
        Mock view = mock(SetQAStatusView.class);
        expects(view, "close");

        SetQAStatusPresenter presenter = new SetQAStatusPresenter((SetQAStatusView) view.proxy(), null, null, null);
        presenter.doClose();
    }

    public void testShouldObserverAndDisplayViewOnDisplay() {
        QAStep[] steps = {};
        User user = new User();

        Mock session = mock(EmfSession.class);
        stub(session, "user", user);

        Mock view = mock(SetQAStatusView.class);
        expectsOnce(view, "display", new Constraint[] { same(steps), same(user) });

        SetQAStatusPresenter presenter = new SetQAStatusPresenter((SetQAStatusView) view.proxy(), steps, null,
                (EmfSession) session.proxy());
        expects(view, "observe");

        presenter.display();
    }
}
