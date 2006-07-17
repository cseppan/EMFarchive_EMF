package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;


public class EditControlStrategyOutputTabPresenter implements EditControlStrategyTabPresenter {

    private EmfSession session;

    public EditControlStrategyOutputTabPresenter(EditControlStrategyOutputTabView view, EmfSession session) {
        this.session = session;
    }

    public void doSave() {
        // NOTE Auto-generated method stub

    }

    public void doExport(ControlStrategy controlStrategy) {
        session.controlStrategyService();
    }

    public void doAnalyze(ControlStrategy controlStrategy, String folder) throws EmfException {
        if(folder==null || folder.length()==0){
            throw new EmfException("Please specify the folder name");
        }
        
    }

}
