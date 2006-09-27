package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.services.qa.QAService;

import org.jmock.Mock;
import org.jmock.core.Constraint;

public class QATabPresenterTest extends EmfMockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() throws EmfException {
        Mock view = mock(QATabView.class);
        EmfDataset dataset = new EmfDataset();
        dataset.setId(6);

        Mock service = mock(QAService.class);
        QAStep[] steps = new QAStep[0];
        service.expects(once()).method("getQASteps").with(same(dataset)).will(returnValue(steps));

        QATabPresenter presenter = new QATabPresenterImpl((QATabView) view.proxy(), dataset, (QAService) service
                .proxy());

        expectsOnce(view, "display", steps);
        expectsOnce(view, "observe", presenter);

        presenter.display();
    }

    public void testShouldDisplayQAStepViewOnView() throws EmfException {
        QAStep step = new QAStep();
        QAStepResult result = new QAStepResult();
        Mock service = mock(QAService.class);
        service.expects(once()).method("getQAStepResult").with(same(step)).will(returnValue(result));
        QATabPresenter presenter = new QATabPresenterImpl(null, null,(QAService) service.proxy());

        
        Mock view = mock(QAStepView.class);
        expectsOnce(view, "display",new Constraint[]{eq(step),eq(result)});

        
        
        presenter.doView(step, (QAStepView) view.proxy());
    }

}
