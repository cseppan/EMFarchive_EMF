package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.editor.DataEditorService;
import gov.epa.emissions.framework.services.qa.QAService;

public class EditableQAStepsPresenterImpl implements EditableQATabPresenter {

    private EditableQATabView view;

    private EmfDataset dataset;

    private EmfSession session;

    public EditableQAStepsPresenterImpl(EmfDataset dataset, EmfSession session, EditableQATabView view) {
        this.dataset = dataset;
        this.session = session;
        this.view = view;
    }

    public void display() throws EmfException {
        QAStep[] steps = qaService().getQASteps(dataset);
        view.display(steps, versions());
        view.observe(this);
    }

    private Version[] versions() throws EmfException {
        DataEditorService service = session.dataEditorService();
        return service.getVersions(dataset.getId());
    }

    private QAService qaService() {
        return session.qaService();
    }

    public void doSave() throws EmfException {
        qaService().update(view.steps());
    }

    public void doAddUsingTemplate(NewQAStepView stepView) {
        stepView.display(dataset, dataset.getDatasetType());
        if (stepView.shouldCreate()) {
            view.add(stepView.qaSteps());
        }
    }

    public void doAddCustomized(NewCustomQAStepView stepView) throws EmfException {
        doAddCustomized(stepView, versions());
    }

    void doAddCustomized(NewCustomQAStepView stepView, Version[] versions) {
        stepView.display(dataset, versions);
        if (stepView.shouldCreate()) {
            view.add(stepView.step());
        }
    }

    public void doSetStatus(QAStatusView statusView) {
        statusView.display();
        if (statusView.shouldSetStatus()) {
            view.setStatus(statusView.qaStepStub());
        }
    }

    public void doPerform(QAStep step, PerformQAStepView performView) {
        PerformQAStepPresenter presenter = new PerformQAStepPresenter(performView, dataset);
        presenter.display(step);
    }

}
