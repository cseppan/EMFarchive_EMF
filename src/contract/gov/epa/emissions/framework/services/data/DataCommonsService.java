package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.Country;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.editor.Revision;

public interface DataCommonsService {
    // Keywords
    Keyword[] getKeywords() throws EmfException;

    // Projects
    Project[] getProjects() throws EmfException;

    void addProject(Project project) throws EmfException;

    // intended use
    IntendedUse[] getIntendedUses() throws EmfException;

    void addIntendedUse(IntendedUse intendedUse) throws EmfException;

    // Regions
    Region[] getRegions() throws EmfException;

    void addRegion(Region region) throws EmfException;

    // Countries
    Country[] getCountries() throws EmfException;

    // Sectors
    Sector[] getSectors() throws EmfException;

    Sector obtainLockedSector(User owner, Sector sector) throws EmfException;

    Sector updateSector(Sector sector) throws EmfException;

    void addSector(Sector sector) throws EmfException;

    Sector releaseLockedSector(Sector sector) throws EmfException;

    // DatasetType
    DatasetType[] getDatasetTypes() throws EmfException;

    void addDatasetType(DatasetType type) throws EmfException;

    DatasetType obtainLockedDatasetType(User owner, DatasetType type) throws EmfException;

    DatasetType updateDatasetType(DatasetType type) throws EmfException;

    DatasetType releaseLockedDatasetType(User owner, DatasetType type) throws EmfException;

    // Status
    Status[] getStatuses(String username) throws EmfException;

    // Revisions
    Revision[] getRevisions(long datasetId) throws EmfException;
    void addRevision(Revision revision) throws EmfException;

    // Notes
    Note[] getNotes(long datasetId) throws EmfException;
    void addNote(Note note) throws EmfException;
    void addNotes(Note[] notes) throws EmfException;

    // Note types
    NoteType[] getNoteTypes() throws EmfException;
}
