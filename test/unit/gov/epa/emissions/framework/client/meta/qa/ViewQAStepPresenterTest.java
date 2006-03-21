package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.framework.services.data.QAStep;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class ViewQAStepPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() {
        Mock view = mock(ViewQAStepView.class);
        
        QAStep step = new QAStep(new QAStepTemplate(), 1);

        ViewQAStepPresenter presenter = new ViewQAStepPresenter((ViewQAStepView) view.proxy());

        view.expects(once()).method("display").with(eq(step));

        presenter.display(step);
    }

}
