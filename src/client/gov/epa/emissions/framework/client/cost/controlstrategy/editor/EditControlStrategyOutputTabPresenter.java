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
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
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

    public void doExport(EmfDataset[] datasets, String folder) throws EmfException {
        view.clearMsgPanel();
        
        if(datasets.length==0){
            throw new EmfException("Please select one or more result datasets");
        }
        validateFolder(folder);
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

    public void doInventory(ControlStrategy controlStrategy, ControlStrategyInputDataset controlStrategyInputDataset) throws EmfException {
        view.clearMsgPanel();
        session.controlStrategyService().createInventory(session.user(), controlStrategy, 
                controlStrategyInputDataset);
    }

    public void doRefresh(ControlStrategyResult[] controlStrategyResults) {
        view.refresh(controlStrategyResults);
        view.recentExportFolder(folder());
    }
    
    public void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset) {
        view.clearMsgPanel();
        PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, session);
        presenter.doDisplay(propertiesView);
    }

    public void doDisplayPropertiesEditor(DatasetPropertiesEditorView editor, EmfDataset detailedResultDataset) throws EmfException {
        view.clearMsgPanel();
        PropertiesEditorPresenter presenter = new PropertiesEditorPresenterImpl(detailedResultDataset, editor, session);
        presenter.doDisplay();
//        editor.setDefaultTab(7);
    }
    
}
