package gov.epa.emissions.framework.client.meta.info;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.PropertiesEditorTabPresenter;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.util.Arrays;
import java.util.List;

public class InfoTabPresenter implements PropertiesEditorTabPresenter {

    private InfoTabView view;

    private EmfDataset dataset;
    
    private List<KeyVal> keys;

    private EmfSession session;

    public InfoTabPresenter(InfoTabView view, EmfDataset dataset, EmfSession session) {
        this.view = view;
        this.dataset = dataset;
        this.keys = Arrays.asList(dataset.getKeyVals());
        this.session = session;
    }

    public void doSave() {
        this.dataset.setKeyVals(keys.toArray(new KeyVal[0]));
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

    public void updateKeyVals(KeyVal[] keys) {
        this.keys = Arrays.asList(keys);
    }

}
