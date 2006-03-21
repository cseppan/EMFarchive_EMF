package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class EditableQAStepsPresenter {

    private EditableQATabView view;

    private EmfDataset dataset;

    public EditableQAStepsPresenter(EmfDataset dataset, EditableQATabView view) {
        this.dataset = dataset;
        this.view = view;
    }

    public void register() {
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
