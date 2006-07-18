package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResult;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.exim.ExImService;


public class EditControlStrategyOutputTabPresenter implements EditControlStrategyTabPresenter {

    private EmfSession session;

    public EditControlStrategyOutputTabPresenter(EmfSession session) {
        this.session = session;
    }

    public void doSave() {
        // NOTE Auto-generated method stub

    }

    public void doExport(ControlStrategy controlStrategy, String folder) throws EmfException {
        validateFolder(folder);
        ExImService service = session.eximService();
        StrategyResult[] strategyResults = controlStrategy.getStrategyResults();
        EmfDataset [] datasets = new EmfDataset[strategyResults.length];
        for (int i = 0; i < strategyResults.length; i++) {
            datasets[i] = (EmfDataset) strategyResults[i].getDetailedResultDataset();
        }
        service.exportDatasetsWithOverwrite(session.user(),datasets,mapToRemote(folder),"Exporting datasets");
    }
    
    private String mapToRemote(String dir) {
        return session.preferences().mapLocalOutputPathToRemote(dir);
    }

    public void doAnalyze(ControlStrategy controlStrategy, String folder) throws EmfException {
        doExport(controlStrategy,folder);
        
    }

    private void validateFolder(String folder) throws EmfException {
        if(folder==null || folder.length()==0){
            throw new EmfException("Please specify a directory to export");
        }
    }

}
