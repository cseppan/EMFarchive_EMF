package gov.epa.emissions.framework.client.meta.summary;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class EditableSummaryTabPresenterImpl implements EditableSummaryTabPresenter {

    private EditableSummaryTabView view;

    private EmfDataset dataset;

    public EditableSummaryTabPresenterImpl(EmfDataset dataset, EditableSummaryTabView view) {
        this.dataset = dataset;
        this.view = view;
    }

    public void doSave() throws EmfException {
        view.save(dataset);
        verifyEmptyName(dataset);

    }

    private void verifyEmptyName(EmfDataset dataset) throws EmfException {
        if (dataset.getName().trim().equals("")) {
            throw new EmfException("Name field should be a non-empty string.");
        }
    }

}
