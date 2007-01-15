package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;

public interface EditableCaseParameterTabView {
    // update with the view contents
    void save(Case caseObj) throws EmfException;
    
    void display();

}
