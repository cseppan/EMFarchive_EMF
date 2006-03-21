package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.qa.QAService;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class QATabPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() throws EmfException {
        Mock view = mock(ViewableQATabView.class);
        EmfDataset dataset = new EmfDataset();
        dataset.setId(6);

        Mock service = mock(QAService.class);
        QAStep[] steps = new QAStep[0];
        service.expects(once()).method("getQASteps").with(same(dataset)).will(returnValue(steps));

        QATabPresenter presenter = new QATabPresenter((ViewableQATabView) view.proxy(), dataset, (QAService) service.proxy());

        view.expects(once()).method("display").with(eq(steps));

        presenter.display();
    }

}
