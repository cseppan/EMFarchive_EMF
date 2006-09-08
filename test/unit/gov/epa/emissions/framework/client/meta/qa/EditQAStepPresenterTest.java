package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.qa.QAService;

import org.jmock.Mock;
import org.jmock.core.Constraint;

public class EditQAStepPresenterTest extends EmfMockObjectTestCase {

    public void testShouldRefreshTabViewAndCloseOnEdit() {
        Mock tabView = mock(EditableQATabView.class);
        expects(tabView, "refresh");

        Mock view = mock(EditQAStepView.class);
        expects(view, "disposeView");

        EditQAStepPresenter presenter = new EditQAStepPresenter((EditQAStepView) view.proxy(), null,
                (EditableQATabView) tabView.proxy(), null);
        presenter.doEdit();
    }

    public void testShouldCloseViewOnClose() {
        Mock view = mock(EditQAStepView.class);
        expects(view, "disposeView");

        EditQAStepPresenter presenter = new EditQAStepPresenter((EditQAStepView) view.proxy(), null, null, null);
        presenter.doClose();
    }

    public void testShouldObserverAndDisplayViewOnDisplay() throws EmfException {
        QAStep step = new QAStep();
        EmfDataset dataset = new EmfDataset();
        User user = new User();

        QAProgram[] programs = {};
        
        Mock view = mock(EditQAStepView.class);
        expectsOnce(view, "display", new Constraint[] { same(step),same(programs), same(dataset), same(user), same("") });
        
        Mock qaService = mock(QAService.class);
        expects(qaService,1,"getQAPrograms",programs);
        
        Mock session = mock(EmfSession.class);
        expects(session,1, "user", user);
        expects(session,1, "qaService",qaService.proxy());
        
        EditQAStepPresenter presenter = new EditQAStepPresenter((EditQAStepView) view.proxy(), dataset, null,
                (EmfSession) session.proxy());
        expects(view, "observe");

        presenter.display(step, "");
    }
}
