package gov.epa.emissions.framework.client.meta.summary;

import java.util.Date;

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
        dataset.setModifiedDateTime(new Date());
        view.save(dataset);
    }

}
