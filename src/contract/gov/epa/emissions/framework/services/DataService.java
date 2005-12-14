package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.framework.EmfException;

public interface DataService {

    // Datasets
    EmfDataset[] getDatasets() throws EmfException;
    void updateDefaultVersion(long datasetId, int lastFinalVersion) throws EmfException;
    void deleteDataset(EmfDataset dataset)throws EmfException ;

    void updateDataset(EmfDataset dataset) throws EmfException;

    // Countries
    Country[] getCountries() throws EmfException;

    void addCountry(Country country) throws EmfException;

    void updateCountry(Country country) throws EmfException;

    // Sectors
    Sector[] getSectors() throws EmfException;

    void addSector(Sector sector) throws EmfException;

    void updateSector(Sector sector) throws EmfException;

}