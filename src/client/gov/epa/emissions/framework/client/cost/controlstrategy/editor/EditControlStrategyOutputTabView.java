package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

public interface EditControlStrategyOutputTabView extends EditControlStrategyTabView {

    void observe(EditControlStrategyOutputTabPresenter presenter);
    
    void export();
    
    void analyze();
}
