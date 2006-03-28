package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;

import org.jmock.Mock;
import org.jmock.core.Constraint;

public class EditQAStepPresenterTest extends EmfMockObjectTestCase {

    public void testShouldRefreshTabViewAndCloseOnEdit() {
        Mock tabView = mock(EditableQATabView.class);
        expects(tabView, "refresh");

        Mock view = mock(EditQAStepView.class);
        expects(view, "close");

        EditQAStepPresenter presenter = new EditQAStepPresenter((EditQAStepView) view.proxy(), null,
                (EditableQATabView) tabView.proxy());
        presenter.doEdit();
    }

    public void testShouldCloseViewOnClose() {
        Mock view = mock(EditQAStepView.class);
        expects(view, "close");

        EditQAStepPresenter presenter = new EditQAStepPresenter((EditQAStepView) view.proxy(), null, null);
        presenter.doClose();
    }

    public void testShouldObserverAndDisplayViewOnDisplay() {
        Mock view = mock(EditQAStepView.class);
        QAStep step = new QAStep();
        EmfDataset dataset = new EmfDataset();
        expectsOnce(view, "display", new Constraint[] { same(step), same(dataset) });

        EditQAStepPresenter presenter = new EditQAStepPresenter((EditQAStepView) view.proxy(), dataset, null);
        expects(view, "observe");

        presenter.display(step);
    }
}
