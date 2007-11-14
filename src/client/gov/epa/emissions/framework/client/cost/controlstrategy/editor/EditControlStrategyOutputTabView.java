package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

public interface EditControlStrategyOutputTabView extends EditControlStrategyTabView {

    void observe(EditControlStrategyOutputTabPresenter presenter);
    
    void export();
    
    void analyze();
    
    void recentExportFolder(String folder);
    
    String getExportFolder();

    void displayAnalyzeTable(String controlStrategyName, String[] fileNames);
    
    void clearMsgPanel();
    
}
