package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.qa.QAService;

import org.jmock.Mock;
import org.jmock.core.Constraint;

public class EditableQAStepsPresenterTest extends EmfMockObjectTestCase {

    public void testShouldObserveViewOnDisplay() throws EmfException {
        Mock view = mock(EditableQATabView.class);

        Mock service = mock(QAService.class);
        EmfDataset dataset = new EmfDataset();
        QAStep[] steps = new QAStep[0];
        stub(service, "getQASteps", dataset, steps);

        EditableQAStepsPresenter presenter = new EditableQAStepsPresenterImpl(dataset, (QAService) service.proxy(),
                (EditableQATabView) view.proxy());
        expectsOnce(view, "observe", presenter);
        expectsOnce(view, "display", steps);

        presenter.display();
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
        expects(newQAStepview, 1, "display", new Constraint[] { same(dataset), same(dataset.getDatasetType()) });
        stub(newQAStepview, "shouldCreate", Boolean.TRUE);
        expects(newQAStepview, 1, "qaSteps", steps);

        Mock tabview = mock(EditableQATabView.class);
        expects(tabview, 1, "add", same(steps));

        EditableQAStepsPresenter presenter = new EditableQAStepsPresenterImpl(dataset, null, (EditableQATabView) tabview
                .proxy());

        presenter.doAdd((NewQAStepView) newQAStepview.proxy());
    }

    public void testShouldSaveQAStepOnSave() throws EmfException {
        EmfDataset dataset = new EmfDataset();
        DatasetType type = new DatasetType();
        dataset.setName("test");
        dataset.setDatasetType(type);

        Mock tabview = mock(EditableQATabView.class);
        expects(tabview, "save");

        EditableQAStepsPresenter presenter = new EditableQAStepsPresenterImpl(null, null, (EditableQATabView) tabview
                .proxy());

        presenter.doSave();
    }

}