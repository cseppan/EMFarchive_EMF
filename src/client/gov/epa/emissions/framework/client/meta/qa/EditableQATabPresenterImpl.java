package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.editor.DataEditorService;
import gov.epa.emissions.framework.services.qa.QAService;

public class EditableQATabPresenterImpl implements EditableQATabPresenter {

    private EditableQATabView view;

    private EmfDataset dataset;

    private EmfSession session;

    public EditableQATabPresenterImpl(EmfDataset dataset, EmfSession session, EditableQATabView view) {
        this.dataset = dataset;
        this.session = session;
        this.view = view;
    }

    public void display() throws EmfException {
        QAStep[] steps = qaService().getQASteps(dataset);
        view.display(dataset, steps, versions());
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
        DatasetType type = dataset.getDatasetType();
        if (type.getQaStepTemplates().length == 0) {
            view.informLackOfTemplatesForAddingNewSteps(type);
            return;
        }

        stepView.display(dataset, type);
        if (stepView.shouldCreate()) {
            view.add(stepView.steps());
        }
    }

    public void doAddCustomized(NewCustomQAStepView stepView) throws EmfException {
        doAddCustomized(stepView, versions());
    }

    void doAddCustomized(NewCustomQAStepView stepView, Version[] versions) {
        stepView.display(dataset, versions, view);
    }

    public void doSetStatus(SetQAStatusView statusView, QAStep[] steps) {
        SetQAStatusPresenter presenter = new SetQAStatusPresenter(statusView, steps, view, session);
        presenter.display();
    }

    public void doEdit(QAStep step, EditQAStepView performView, String versionName) {
        EditQAStepPresenter presenter = new EditQAStepPresenter(performView, dataset, view, session);
        presenter.display(step, versionName);
    }

}
