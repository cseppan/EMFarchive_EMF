package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class ViewQAStepPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() {
        Mock view = mock(QAStepView.class);
        
        QAStep step = new QAStep(new QAStepTemplate(), 1);
        QAStepResult qaStepResult = new QAStepResult();
        ViewQAStepPresenter presenter = new ViewQAStepPresenter((QAStepView) view.proxy());

        view.expects(once()).method("display").with(eq(step),eq(qaStepResult));

        
        presenter.display(step, qaStepResult );
    }

}
