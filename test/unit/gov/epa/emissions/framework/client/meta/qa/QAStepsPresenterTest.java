package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;

import org.jmock.Mock;

public class QAStepsPresenterTest extends EmfMockObjectTestCase {

    public void testShouldObserveViewOnDisplay() {
        Mock view = mock(QATabView.class);

        QAStepsPresenter presenter = new QAStepsPresenter(null, (QATabView) view.proxy());
        expectsOnce(view, "observe", presenter);

        presenter.register();
    }

    public void testShouldAddQAStepToViewOnAdd() {
        EmfDataset dataset = new EmfDataset();
        DatasetType type = new DatasetType();
        dataset.setName("test");
        dataset.setDatasetType(type);

        QAStepTemplate template = new QAStepTemplate();
        QAStep step1 = new QAStep(template, 0);
        QAStep step2 = new QAStep(template, 1);
        QAStep[] steps = { step1, step2 };

        Mock newQAStepview = mock(NewQAStepView.class);
        expects(newQAStepview, 1, "display", same(dataset.getDatasetType()));
        stub(newQAStepview, "shouldCreate", Boolean.TRUE);
        expects(newQAStepview, 1, "qaSteps", steps);

        Mock tabview = mock(QATabView.class);
        expects(tabview, 1, "add", same(steps));

        QAStepsPresenter presenter = new QAStepsPresenter(dataset, (QATabView) tabview.proxy());

        presenter.doAdd((NewQAStepView) newQAStepview.proxy(), dataset);
    }

    public void testShouldSaveQAStepOnSave() {
        EmfDataset dataset = new EmfDataset();
        DatasetType type = new DatasetType();
        dataset.setName("test");
        dataset.setDatasetType(type);

        Mock tabview = mock(QATabView.class);
        expects(tabview, "save");

        QAStepsPresenter presenter = new QAStepsPresenter(dataset, (QATabView) tabview.proxy());

        presenter.doSave();
    }

}