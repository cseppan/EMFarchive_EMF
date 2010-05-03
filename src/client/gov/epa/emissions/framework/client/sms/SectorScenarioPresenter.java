package gov.epa.emissions.framework.client.sms;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.sms.SectorScenario;

public class SectorScenarioPresenter {

    private EmfSession session;

    private SectorScenarioView view;
    
    public SectorScenarioPresenter(SectorScenarioView view, EmfSession session) {
        this.session = session;
        this.view = view;
    }

    public void display() throws Exception {
        view.observe(this);

        view.display();
    }

    public void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset) throws EmfException {
        view.clearMessage();

        PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, session);
        presenter.doDisplay(propertiesView);
    }
    
    public EmfSession getSession(){
        return session; 
    }

    public EmfDataset getDatasets(int id) throws EmfException{
        return session.dataService().getDataset(id);
    }
    
    public EmfDataset[] getDatasets(DatasetType datasetType) throws EmfException{
        return session.dataService().getDatasets(datasetType);
    }
    
    public DatasetType getDatasetType(String name) throws EmfException{
        return session.dataCommonsService().getDatasetType(name);
    }
    
    public Version[] getVersions(EmfDataset dataset) throws EmfException 
    {
        if (dataset == null) {
            return new Version[0];
        }
        return session.dataEditorService().getVersions(dataset.getId());
    }

    public int addSectorScenario(SectorScenario sectorScenario) throws EmfException {
        return session.sectorScenarioService().addSectorScenario(sectorScenario);
    }

}