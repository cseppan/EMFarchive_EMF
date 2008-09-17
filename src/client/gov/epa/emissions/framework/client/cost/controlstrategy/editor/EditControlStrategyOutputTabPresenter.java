package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesEditorView;
import gov.epa.emissions.framework.client.meta.PropertiesEditorPresenter;
import gov.epa.emissions.framework.client.meta.PropertiesEditorPresenterImpl;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.StrategyType;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.exim.ExImService;

public class EditControlStrategyOutputTabPresenter implements EditControlStrategyTabPresenter {

    private EmfSession session;

    private EditControlStrategyOutputTabView view;
    
    private static String lastFolder = null;

    public EditControlStrategyOutputTabPresenter(EmfSession session, EditControlStrategyOutputTabView view) {
        this.session = session;
        this.view = view;
    }

    public void doSave(ControlStrategy controlStrategy) throws EmfException {
        view.save(controlStrategy);
    }

    public void doExport(EmfDataset[] datasets, String folder) throws EmfException {
        view.clearMsgPanel();
        
        if(datasets.length==0){
            throw new EmfException("Please select one or more result datasets");
        }
//        validateFolder(folder);
        session.setMostRecentExportFolder(folder);
        ExImService service = session.eximService();
        Version[] versions = new Version [datasets.length];
        for (int i = 0; i < datasets.length; i++) {
            versions[i] = service.getVersion(datasets[i], datasets[i].getDefaultVersion());
        }
        service.exportDatasetsWithOverwrite(session.user(), datasets, versions, folder, "Exporting datasets");
    }

//    private String mapToRemote(String dir) {
//        return session.preferences().mapLocalOutputPathToRemote(dir);
//    }

    public void doAnalyze(String controlStrategyName, EmfDataset[] datasets) throws EmfException {
        view.clearMsgPanel();
        
        if(datasets.length==0){
            throw new EmfException("Please select one or more result datasets");
        }
        String[]  fileNames = new String[datasets.length];
        for (int i = 0; i < datasets.length; i++) {
            int datasetId = datasets[i].getId();
            fileNames[i] = session.loggingService().getLastExportedFileName(datasetId);
        }
        view.displayAnalyzeTable(controlStrategyName,fileNames);
    }

//    private void validateFolder(String folder) throws EmfException {
//        File dir = new File(folder);
//        if (!dir.isDirectory()) 
//            throw new EmfException("Please specify a directory to export");
//    }

    public void doDisplay(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults) throws EmfException {
        view.observe(this);
        view.display(controlStrategy, controlStrategyResults);
//        view.recentExportFolder(folder());
    }

    public void setLastFolder(String folder){
        lastFolder = folder; 
    }

    public String folder() {
        String dir = "";
        try {
            dir = (lastFolder != null) ? lastFolder : defaultFolder();
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        return dir;
    }

    private String defaultFolder() throws EmfException {
        return session.controlStrategyService().getDefaultExportDirectory();
    }

    public void doInventory(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults) throws EmfException {
        view.clearMsgPanel();
        session.controlStrategyService().createInventories(session.user(), controlStrategy, 
                controlStrategyResults);
    }

    public void doRefresh(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults) {
        view.refresh(controlStrategy, controlStrategyResults);
//        view.recentExportFolder(folder());
    }
    
    public void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset) throws EmfException {
        view.clearMsgPanel();
        PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, session);
        presenter.doDisplay(propertiesView);
    }

    public void doDisplayPropertiesEditor(DatasetPropertiesEditorView editor, EmfDataset dataset) throws EmfException {
        view.clearMsgPanel();
//        //make sure the dataset still exists, it could have been removed and the client might not of/
//        //refreshed their view, so lets check for the existence of the dataset
//        dataset = 
        PropertiesEditorPresenter presenter = new PropertiesEditorPresenterImpl(dataset, editor, session);
        presenter.doDisplay();
//        editor.setDefaultTab(7);
    }

    public void doChangeStrategyType(StrategyType strategyType) {
        // NOTE Auto-generated method stub
        
    }
}
