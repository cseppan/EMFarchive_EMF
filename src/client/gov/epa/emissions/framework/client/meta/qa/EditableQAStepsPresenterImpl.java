package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.qa.QAService;

public class EditableQAStepsPresenterImpl implements EditableQAStepsPresenter {

    private EditableQATabView view;

    private EmfDataset dataset;

    private QAService service;

    public EditableQAStepsPresenterImpl(EmfDataset dataset, QAService service, EditableQATabView view) {
        this.dataset = dataset;
        this.service = service;
        this.view = view;
    }

    public void display() throws EmfException {
        view.display(service.getQASteps(dataset));
        view.observe(this);
    }

    public void doSave() throws EmfException {
        view.save();
    }

    public void doAdd(NewQAStepView stepview) {
        stepview.display(dataset, dataset.getDatasetType());
        if (stepview.shouldCreate()) {
            view.add(stepview.qaSteps());
        }
    }

}
