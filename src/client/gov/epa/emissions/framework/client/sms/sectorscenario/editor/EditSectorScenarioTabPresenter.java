package gov.epa.emissions.framework.client.sms.sectorscenario.editor;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.sms.SectorScenario;

public interface EditSectorScenarioTabPresenter {
    void doSave() throws EmfException;
    
    void doRefresh(SectorScenario sectorScenario) throws EmfException;
}
