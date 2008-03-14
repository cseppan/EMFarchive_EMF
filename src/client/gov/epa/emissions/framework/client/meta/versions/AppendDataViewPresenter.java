package gov.epa.emissions.framework.client.meta.versions;

import java.util.ArrayList;
import java.util.List;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class AppendDataViewPresenter {
    
    private EmfDataset dataset;
    
    private EmfSession session;
    
    private AppendDataWindowView view;

    public AppendDataViewPresenter(EmfDataset dataset, AppendDataWindowView view, EmfSession session) {
        this.dataset = dataset;
        this.session = session;
        this.view = view;
    }
    
    public void displayView() {
        view.observe(this);
        view.display();
    }
    
    public void appendData(EmfDataset sourceDataset) throws EmfException {
        throw new EmfException("Under construction...");
    }
    
    public Version[] getVersions(int dsId) throws EmfException {
        return session.dataEditorService().getVersions(dsId);
    }
    
    public Version[] getTargetDatasetNonFinalVersions() throws EmfException {
        List<Version> nonFinalVersions = new ArrayList<Version>();
        nonFinalVersions.add(new Version());
        
        Version[] allVersions = getVersions(dataset.getId());
        
        for(Version version : allVersions)
            if (!version.isFinalVersion())
                nonFinalVersions.add(version);
        
        return nonFinalVersions.toArray(new Version[0]);
    }
    
    public EmfDataset getDataset() {
        return this.dataset;
    }
    
    public EmfSession getSession() {
        return this.session;
    }
}
