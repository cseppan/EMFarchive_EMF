package gov.epa.emissions.framework.client.data.editor;

import gov.epa.emissions.framework.client.EmfSession;

public class FindReplaceViewPresenter {
    
    private FindReplaceWindowView view;
    
    public FindReplaceViewPresenter(FindReplaceWindowView view, EmfSession session) {
        this.view = view;
    }
    
    public void displayView() {
        view.observe(this);
        view.display();
    }
    
}
