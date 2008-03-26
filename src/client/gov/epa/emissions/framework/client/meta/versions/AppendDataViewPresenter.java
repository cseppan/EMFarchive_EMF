package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.data.viewer.DataView;
import gov.epa.emissions.framework.client.data.viewer.DataViewPresenter;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.editor.Revision;

import java.util.ArrayList;
import java.util.List;

public class AppendDataViewPresenter {
    
    private EmfDataset dataset;
    
    private EmfSession session;
    
    private AppendDataWindowView view;

    public AppendDataViewPresenter(EmfDataset dataset, AppendDataWindowView view, EmfSession session) {
        //super(dataset, session);
        this.dataset=dataset;
        this.session = session;
        this.view = view;
    }
    
    public void displayView() {
        view.observe(this);
        view.display();
    }
    
    public void appendData(int srcDSid, int srcDSVersion, String filter, int targetDSid, int targetDSVersion, int startLineNum, int endLineNum) throws EmfException {
        session.dataService().appendData(srcDSid, srcDSVersion, filter, targetDSid, targetDSVersion, startLineNum, endLineNum);
    }
    
    public Version[] getVersions(int dsId) throws EmfException {
        return session.dataEditorService().getVersions(dsId);
    }
    
    public EmfDataset getDataset(int datasetId) throws EmfException{
        return session.dataService().getDataset(datasetId);
    }
    
    public Version[] getTargetDatasetNonFinalVersions() throws EmfException {
        List<Version> nonFinalVersions = new ArrayList<Version>();
        
        Version[] allVersions = getVersions(dataset.getId());
        
        for(Version version : allVersions)
            if (!version.isFinalVersion())
                nonFinalVersions.add(version);
        
        return nonFinalVersions.toArray(new Version[0]);
    }
    
    public void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset) throws EmfException {
        PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, session);
        presenter.doDisplay(propertiesView);
    }
    
    public void doView(Version version, String table, DataView view) throws EmfException {
        if (!version.isFinalVersion())
            throw new EmfException("Cannot view a Version that is not Final. Please choose edit for Version "+
                    version.getName());

        DataViewPresenter presenter = new DataViewPresenter(dataset, version, table, view, session);
        presenter.display();
    }

    public boolean isLineBased(){
        String importerClass = dataset.getDatasetType().getImporterClassName();
        
      return importerClass.equals("gov.epa.emissions.commons.io.generic.LineImporter");
    }
    
    public EmfDataset getDataset() {
        return this.dataset;
    }
    
    public EmfSession getSession() {
        return this.session;
    }
    
    public void addRevision(Revision revision) throws EmfException {
        session.dataCommonsService().addRevision(revision);
    }
    
    public void checkIfDeletable(EmfDataset dataset) throws EmfException {
        String currentUser = session.user().getUsername();
        
        if (currentUser.equals(dataset.getCreator()))
            throw new EmfException("Current user is not the creator.");
               
        session.dataService().checkIfDeletable(session.user(), dataset.getId());
    }

    public EmfDataset getDataset(String sourceDSName) throws EmfException {
        return session.dataService().getDataset(sourceDSName);
    }
}
