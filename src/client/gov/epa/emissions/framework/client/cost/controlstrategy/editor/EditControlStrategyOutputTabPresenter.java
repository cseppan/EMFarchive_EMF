package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.preference.UserPreference;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResult;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.exim.ExImService;

import java.io.File;

public class EditControlStrategyOutputTabPresenter implements EditControlStrategyTabPresenter {

    private EmfSession session;

    private EditControlStrategyOutputTabView view;

    public EditControlStrategyOutputTabPresenter(EmfSession session, EditControlStrategyOutputTabView view) {
        this.session = session;
        this.view = view;
    }

    public void doSave() {
        // NOTE Auto-generated method stub

    }

    public void doExport(ControlStrategy controlStrategy, String folder) throws EmfException {
        validateFolder(folder);
        session.setMostRecentExportFolder(folder);
        ExImService service = session.eximService();
        StrategyResult[] strategyResults = controlStrategy.getStrategyResults();
        EmfDataset[] datasets = new EmfDataset[strategyResults.length];
        for (int i = 0; i < strategyResults.length; i++) {
            datasets[i] = (EmfDataset) strategyResults[i].getDetailedResultDataset();
        }
        service.exportDatasetsWithOverwrite(session.user(), datasets, mapToRemote(folder), "Exporting datasets");
    }

    private String mapToRemote(String dir) {
        return session.preferences().mapLocalOutputPathToRemote(dir);
    }

    public void doAnalyze(String controlStrategyName, StrategyResult[] strategyResults) throws EmfException {
        if(strategyResults.length==0){
            throw new EmfException("Please select one or more result datasets");
        }
        String[]  fileNames = new String[strategyResults.length];
        UserPreference preference = session.preferences();
        for (int i = 0; i < strategyResults.length; i++) {
            int datasetId = strategyResults[i].getDetailedResultDataset().getId();
            String fileNameOnServer = session.loggingService().getLastExportedFileName(datasetId);
            fileNames[i] = preference.mapRemoteOutputPathToLocal(fileNameOnServer);
        }
        view.displayAnalyzeTable(controlStrategyName,fileNames);
    }

    private void validateFolder(String folder) throws EmfException {
        if (folder == null || folder.length() == 0) {
            throw new EmfException("Please specify a directory to export");
        }
    }

    public void doDisplay() {
        view.observe(this);
        view.recentExportFolder(folder());
    }

    private String folder() {
        String lastFolder = session.getMostRecentExportFolder();
        return (lastFolder != null) ? lastFolder : defaultFolder();
    }

    private String defaultFolder() {
        String folder = session.preferences().outputFolder();
        if (!new File(folder).isDirectory())
            folder = "";// default, if unspecified

        return folder;
    }

    public void doInventory(ControlStrategy controlStrategy) throws EmfException {
        session.controlStrategyService().createInventory(session.user(),controlStrategy);
    }

}
