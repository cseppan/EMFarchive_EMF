package gov.epa.emissions.framework.services.casemanagement;

import java.util.List;

import gov.epa.emissions.framework.services.EmfException;

public interface EMFCaseFile {

    void readAll() throws EmfException;
    
    void read(List<String> attributes) throws EmfException;
    
    String getAttributeValue(String attribute) throws EmfException;
    
    String getMessages();
}
