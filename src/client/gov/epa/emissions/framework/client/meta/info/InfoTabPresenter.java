package gov.epa.emissions.framework.client.meta.info;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.ExternalSource;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.PropertiesEditorTabPresenter;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class InfoTabPresenter implements PropertiesEditorTabPresenter {

    private InfoTabView view;

    private EmfDataset dataset;
    
    private EmfSession session;

    private List<KeyVal> keys = new ArrayList<KeyVal>();

    private List<ExternalSource> srcs = new ArrayList<ExternalSource>();

    public InfoTabPresenter(InfoTabView view, EmfDataset dataset, EmfSession session) {
        this.view = view;
        this.dataset = dataset;
        this.session = session;
        this.keys = Arrays.asList(dataset.getKeyVals());
        this.srcs = Arrays.asList(dataset.getExternalSources());
    }

    public void doSave() {
        this.dataset.setKeyVals(keys.toArray(new KeyVal[0]));
        this.dataset.setExternalSources(srcs.toArray(new ExternalSource[0]));
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

    public void updateExternalSources(KeyVal[] keys, ExternalSource[] srcs) {
        this.keys = Arrays.asList(keys);
        this.srcs = Arrays.asList(srcs);
        view.displayExternalSources(dataset.getExternalSources());
    }

}
