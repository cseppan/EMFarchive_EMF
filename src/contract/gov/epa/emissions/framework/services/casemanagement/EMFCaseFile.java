package gov.epa.emissions.framework.services.casemanagement;

import java.util.List;

import gov.epa.emissions.framework.services.EmfException;

public interface EMFCaseFile {

    void readAll() throws EmfException;
    
    void read(List<String> parameters) throws EmfException;
    
    String[] getAttributes() throws EmfException;
    
    String[] getAttributeValues(String attribute) throws EmfException;
}
