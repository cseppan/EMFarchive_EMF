package gov.epa.emissions.framework.client.sms.sectorscenario.editor;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.sms.SectorScenario;

public interface EditSectorScenarioTabView {
    
    void save(SectorScenario sectorScenario);
    //void run(SectorScenario sectorScenario) throws EmfException;
    void refresh(SectorScenario sectorScenario) throws EmfException;
 
//  //void notifyScenarioRun(SectorScenario sectorScenario);
    
//    void showError(String message);
//      
//    void showRemindingMessage(String msg);
//    

    
}
