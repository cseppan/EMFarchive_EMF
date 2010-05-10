package gov.epa.emissions.framework.client.sms.sectorscenario.viewer;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.sms.sectorscenario.editor.EditSectorScenarioPresenterImpl;
import gov.epa.emissions.framework.services.sms.SectorScenario;

public class ViewSectorScenarioPresenterImpl  extends EditSectorScenarioPresenterImpl implements ViewSectorScenarioPresenter {

    //private ViewSectorScenarioView view;

    public ViewSectorScenarioPresenterImpl(SectorScenario sectorScenario, EmfSession session, 
            ViewSectorScenarioView view) {
        super(sectorScenario, session, view);
        //this.view = view; 
    }

    public void doDisplay() {
        super.doDisplay();
        ((ViewSectorScenarioView)view).viewOnly();
    }


    public void doRefresh() {
        super.doRefresh();
    }

    public void viewOnly() {
       // 
    }

}
