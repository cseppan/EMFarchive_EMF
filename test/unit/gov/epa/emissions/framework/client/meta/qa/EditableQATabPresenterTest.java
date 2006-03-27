package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.editor.DataEditorService;
import gov.epa.emissions.framework.services.qa.QAService;

import org.jmock.Mock;
import org.jmock.core.Constraint;

public class EditableQATabPresenterTest extends EmfMockObjectTestCase {

    public void testShouldObserveViewOnDisplay() throws EmfException {
        Mock view = mock(EditableQATabView.class);

        Mock qaService = mock(QAService.class);
        EmfDataset dataset = new EmfDataset();
        QAStep[] steps = new QAStep[0];
        stub(qaService, "getQASteps", dataset, steps);

        Mock dataEditorService = mock(DataEditorService.class);
        Version[] versions = new Version[0];
        stub(dataEditorService, "getVersions", versions);

        Mock session = mock(EmfSession.class);
        stub(session, "qaService", qaService.proxy());
        stub(session, "dataEditorService", dataEditorService.proxy());

        EditableQATabPresenter presenter = new EditableQAStepsPresenterImpl(dataset, (EmfSession) session.proxy(),
                (EditableQATabView) view.proxy());
        expectsOnce(view, "observe", presenter);
        expectsOnce(view, "display", new Constraint[] { same(steps), same(versions) });

        presenter.display();
    }

    public void testShouldAddNewQAStepOnAddUsingTemplate() {
        EmfDataset dataset = new EmfDataset();
        DatasetType type = new DatasetType();
        dataset.setName("test");
        dataset.setDatasetType(type);

        QAStep[] steps = { new QAStep(), new QAStep() };

        Mock newQAStepview = mock(NewQAStepView.class);
        expects(newQAStepview, 1, "display", new Constraint[] { same(dataset), same(dataset.getDatasetType()) });
        stub(newQAStepview, "shouldCreate", Boolean.TRUE);
        expects(newQAStepview, 1, "qaSteps", steps);

        Mock tabview = mock(EditableQATabView.class);
        expects(tabview, 1, "add", same(steps));

        EditableQATabPresenter presenter = new EditableQAStepsPresenterImpl(dataset, null, (EditableQATabView) tabview
                .proxy());

        presenter.doAddUsingTemplate((NewQAStepView) newQAStepview.proxy());
    }

    public void testShouldAddNewQAStepOnAddCustomized() {
        EmfDataset dataset = new EmfDataset();
        dataset.setName("test");

        Version[] versions = {};

        QAStep step = new QAStep();
        Mock newQAStepview = mock(NewCustomQAStepView.class);
        expects(newQAStepview, 1, "display", new Constraint[] { same(dataset), eq(versions) });
        stub(newQAStepview, "shouldCreate", Boolean.TRUE);
        expects(newQAStepview, 1, "step", step);

        Mock tabview = mock(EditableQATabView.class);
        expects(tabview, 1, "add", same(step));

        EditableQAStepsPresenterImpl presenter = new EditableQAStepsPresenterImpl(dataset, null,
                (EditableQATabView) tabview.proxy());

        presenter.doAddCustomized((NewCustomQAStepView) newQAStepview.proxy(), versions);
    }

    public void testShouldUpdateQAStepOnPerform() {
        EmfDataset dataset = new EmfDataset();
        QAStep step = new QAStep();

        Mock view = mock(PerformQAStepView.class);
        expectsOnce(view, "display", new Constraint[]{same(step), same(dataset)});
        expects(view, "observe");

        EditableQAStepsPresenterImpl presenter = new EditableQAStepsPresenterImpl(dataset, null, null);

        presenter.doPerform(step, (PerformQAStepView) view.proxy());
    }

    public void testShouldSaveQAStepOnSave() throws EmfException {
        EmfDataset dataset = new EmfDataset();
        DatasetType type = new DatasetType();
        dataset.setName("test");
        dataset.setDatasetType(type);

        Mock qaService = mock(QAService.class);
        QAStep[] steps = new QAStep[0];
        Mock view = mock(EditableQATabView.class);
        stub(view, "steps", steps);
        expectsOnce(qaService, "update", steps);

        Mock session = mock(EmfSession.class);
        stub(session, "qaService", qaService.proxy());

        EditableQATabPresenter presenter = new EditableQAStepsPresenterImpl(null, (EmfSession) session.proxy(),
                (EditableQATabView) view.proxy());

        presenter.doSave();
    }

    public void testShouldSetQAStepStatusToViewOnDoSetStatus() {
        QAStep step1 = new QAStep();

        Mock qaStatusView = mock(QAStatusView.class);
        expects(qaStatusView, 1, "display");
        stub(qaStatusView, "shouldSetStatus", Boolean.TRUE);
        expects(qaStatusView, 1, "qaStepStub", step1);

        Mock tabview = mock(EditableQATabView.class);
        expects(tabview, 1, "setStatus", same(step1));

        EditableQATabPresenter presenter = new EditableQAStepsPresenterImpl(null, null, (EditableQATabView) tabview
                .proxy());

        presenter.doSetStatus((QAStatusView) qaStatusView.proxy());
    }

}