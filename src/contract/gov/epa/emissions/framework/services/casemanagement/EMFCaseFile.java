package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.framework.services.EmfException;

public interface EMFCaseFile {

    void read() throws EmfException;
    
    String[] getAttributes() throws EmfException;
    
    String[] getAttributeValues(String attribute) throws EmfException;
}
