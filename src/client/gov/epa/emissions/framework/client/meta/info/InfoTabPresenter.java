package gov.epa.emissions.framework.client.meta.info;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.ExternalSource;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.PropertiesEditorTabPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class InfoTabPresenter implements PropertiesEditorTabPresenter {

    private InfoTabView view;

    private EmfDataset dataset;
    
    private EmfSession session;

    public InfoTabPresenter(InfoTabView view, EmfDataset dataset, EmfSession session) {
        this.view = view;
        this.dataset = dataset;
        this.session = session;
        view.observe(this);
    }

    public void doSave() {
        //NOTE: don't need to do anything, dataset's been updated
    }

    public void doDisplay() throws EmfException {
        DatasetType type = dataset.getDatasetType();

        if (!type.isExternal())
            view.displayInternalSources(dataset.getInternalSources());
        else
            view.displayExternalSources(getExternalSrcs(dataset.getId(), -1));
    }

    public EmfDataset getDataset() {
        return dataset;
    }

    public EmfSession getSession() {
        return session;
    }

    public void refreshExternalSources() throws EmfException {
        view.displayExternalSources(getExternalSrcs(dataset.getId(), -1));
    }
    
    public ExternalSource[] getExternalSrcs(int dsId, int limit) throws EmfException {
        return session.dataService().getExternalSources(dsId, limit);
    }

}
