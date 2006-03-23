package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.qa.QAService;

public class EditableQAStepsPresenterImpl implements EditableQAStepsPresenter {

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
        Version[] versions = session.dataEditorService().getVersions(dataset.getId());
        
        view.display(steps, versions);
        view.observe(this);
    }

    private QAService qaService() {
        return session.qaService();
    }

    public void doSave() throws EmfException {
        qaService().update(view.steps());
    }

    public void doAdd(NewQAStepView stepview) {
        stepview.display(dataset, dataset.getDatasetType());
        if (stepview.shouldCreate()) {
            view.add(stepview.qaSteps());
        }
    }

}
