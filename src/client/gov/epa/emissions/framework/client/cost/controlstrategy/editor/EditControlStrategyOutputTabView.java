package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

public interface EditControlStrategyOutputTabView extends EditControlStrategyTabView {

    void observe(EditControlStrategyOutputTabPresenter presenter);
    
    void export();
    
    void analyze();
    
    void recentExportFolder(String folder);

    void displayAnalyzeTable(String controlStrategyName, String[] fileNames);
    
}
