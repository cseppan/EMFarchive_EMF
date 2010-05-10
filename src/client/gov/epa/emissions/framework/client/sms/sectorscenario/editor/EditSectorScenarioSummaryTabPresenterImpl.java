package gov.epa.emissions.framework.client.sms.sectorscenario.editor;

import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.sms.SectorScenario;

public class EditSectorScenarioSummaryTabPresenterImpl implements EditSectorScenarioSummaryTabPresenter {
    
    protected EmfSession session;
    
    private EditSectorScenarioSummaryTabView view;
    //private int sectorScenarioId;
    //private EditSectorScenarioPresenter editPresenter;
    
    public EditSectorScenarioSummaryTabPresenterImpl(EmfSession session, EditSectorScenarioSummaryTabView view) {
        //this.sectorScenarioId = sectorScenarioId;
        this.session = session;
        this.view = view;
    }

    public void doSave(){
        view.save();
    }

    public Project[] getProjects() throws EmfException {
       return session.dataCommonsService().getProjects();
    }

    public void doRefresh(SectorScenario sectorScenario) throws EmfException {
        view.refresh(sectorScenario);     
    }

}
