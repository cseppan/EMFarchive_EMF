package gov.epa.emissions.framework.client.sms.sectorscenario.editor;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.sms.SectorScenario;

public interface EditSectorScenarioView extends ManagedView {

    void observe(EditSectorScenarioPresenter presenter);

    void display(SectorScenario sectorScenario);
    
    //void refresh(SectorScenario sectorScenario);
    
    void notifyLockFailure(SectorScenario sectorScenario);

    void notifyEditFailure(SectorScenario sectorScenario);

    //void notifyStrategyTypeChange(SectorScenario sectorScenario);
    
    void signalChanges();

    void stopRun();
    
    //void enableButtons(boolean enable);
    
}
