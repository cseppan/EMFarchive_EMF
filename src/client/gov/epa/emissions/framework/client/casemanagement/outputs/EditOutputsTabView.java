package gov.epa.emissions.framework.client.casemanagement.outputs;


public interface EditOutputsTabView {
    
    void display();

    void refresh();

    void observe(EditOutputsTabPresenterImpl editOutputsTabPresenterImpl);

    void clearMessage();
    
}
