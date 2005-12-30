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

    Sector getSectorLock(User user, Sector sector) throws EmfException;

    Sector updateSector(User user, Sector sector) throws EmfException;

    Sector releaseSectorLock(User user, Sector sector) throws EmfException;

    // DatasetType
    DatasetType[] getDatasetTypes() throws EmfException;

    DatasetType getDatasetTypeLock(User user, DatasetType type) throws EmfException;

    DatasetType updateDatasetType(User user, DatasetType type) throws EmfException;

    DatasetType releaseDatasetTypeLock(User user, DatasetType type) throws EmfException;

}
