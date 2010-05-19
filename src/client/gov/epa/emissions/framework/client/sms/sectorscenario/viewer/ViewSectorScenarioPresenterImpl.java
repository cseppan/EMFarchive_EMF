package gov.epa.emissions.framework.client.sms.sectorscenario.viewer;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.sms.sectorscenario.editor.EditSectorScenarioPresenterImpl;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.sms.SectorScenario;

public class ViewSectorScenarioPresenterImpl  extends EditSectorScenarioPresenterImpl implements ViewSectorScenarioPresenter {

    //private ViewSectorScenarioView view;

    public ViewSectorScenarioPresenterImpl(SectorScenario sectorScenario, EmfSession session, 
            ViewSectorScenarioView view) {
        super(sectorScenario, session, view);
        //this.view = view; 
    }

    public void doDisplay() throws EmfException {
        super.doDisplay();
        ((ViewSectorScenarioView)view).viewOnly();
    }


    public void doRefresh() {
        try {
            super.doRefresh();
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void viewOnly() {
       // 
    }

}
