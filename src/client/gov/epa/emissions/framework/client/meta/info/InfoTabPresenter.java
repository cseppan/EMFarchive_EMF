package gov.epa.emissions.framework.client.meta.info;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.PropertiesEditorTabPresenter;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class InfoTabPresenter implements PropertiesEditorTabPresenter {

    private InfoTabView view;

    private EmfDataset dataset;
    
    private EmfSession session;

    public InfoTabPresenter(InfoTabView view, EmfDataset dataset, EmfSession session) {
        this.view = view;
        this.dataset = dataset;
        this.session = session;
    }

    public void doSave() {
        //NOTE: don't need to do anything, dataset's been updated
    }

    public void doDisplay() {
        view.observe(this);
        DatasetType type = dataset.getDatasetType();

        if (!type.isExternal())
            view.displayInternalSources(dataset.getInternalSources());
        else
            view.displayExternalSources(dataset.getExternalSources());
    }

    public EmfDataset getDataset() {
        return dataset;
    }

    public EmfSession getSession() {
        return session;
    }

    public void updateExternalSources() {
        view.displayExternalSources(dataset.getExternalSources());
    }

}
