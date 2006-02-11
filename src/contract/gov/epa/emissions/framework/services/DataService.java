package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;

public interface DataService {

    EmfDataset[] getDatasets() throws EmfException;
    void addDataset(EmfDataset dataset) throws EmfException;

    EmfDataset obtainLockedDataset(User owner, EmfDataset dataset) throws EmfException;

    EmfDataset releaseLockedDataset(EmfDataset locked) throws EmfException;

    EmfDataset updateDataset(EmfDataset dataset) throws EmfException;

}