package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;

public interface DataCommonsService {
    // Keywords
    Keyword[] getKeywords() throws EmfException;

    // Countries
    Country[] getCountries() throws EmfException;

    void addCountry(Country country) throws EmfException;

    void updateCountry(Country country) throws EmfException;

    // Sectors
    Sector[] getSectors() throws EmfException;

    Sector obtainLockedSector(User owner, Sector sector) throws EmfException;

    Sector updateSector(Sector sector) throws EmfException;

    Sector releaseLockedSector(Sector sector) throws EmfException;

    // DatasetType
    DatasetType[] getDatasetTypes() throws EmfException;

    DatasetType obtainLockedDatasetType(User owner, DatasetType type) throws EmfException;

    DatasetType updateDatasetType(User owner, DatasetType type) throws EmfException;

    DatasetType releaseLockedDatasetType(User owner, DatasetType type) throws EmfException;

}
