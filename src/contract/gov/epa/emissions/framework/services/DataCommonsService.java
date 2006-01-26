package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.io.Country;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.commons.io.Project;
import gov.epa.emissions.commons.io.Region;
import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;

public interface DataCommonsService {
    // Keywords
    Keyword[] getKeywords() throws EmfException;

    //Projects
    Project[] getProjects() throws EmfException;
    void addProject(Project project) throws EmfException;

    // intended use
    IntendedUse[] getIntendedUses() throws EmfException;
    void addIntendedUse(IntendedUse intendedUse) throws EmfException;
    
    //Regions
    Region[] getRegions() throws EmfException;
    void addRegion(Region region) throws EmfException;
    
    // Countries
    Country[] getCountries() throws EmfException;

    // Sectors
    Sector[] getSectors() throws EmfException;

    Sector obtainLockedSector(User owner, Sector sector) throws EmfException;

    Sector updateSector(Sector sector) throws EmfException;

    Sector releaseLockedSector(Sector sector) throws EmfException;

    // DatasetType
    DatasetType[] getDatasetTypes() throws EmfException;

    DatasetType obtainLockedDatasetType(User owner, DatasetType type) throws EmfException;

    DatasetType updateDatasetType(DatasetType type) throws EmfException;

    DatasetType releaseLockedDatasetType(User owner, DatasetType type) throws EmfException;

    // Status
    Status[] getStatuses(String username) throws EmfException;

}
