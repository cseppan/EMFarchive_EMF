package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.data.Country;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.IntendedUse;
import gov.epa.emissions.framework.services.data.Note;
import gov.epa.emissions.framework.services.data.NoteType;
import gov.epa.emissions.framework.services.editor.Revision;

public class DataCommonsServiceTransport implements DataCommonsService {
    private EmfMappings mappings;

    private CallFactory callFactory;

    public DataCommonsServiceTransport(String endPoint) {
        callFactory = new CallFactory(endPoint);
        mappings = new EmfMappings();
    }

    private EmfCall call() throws EmfException {
        return callFactory.createCall("DataCommons Service");
    }

    public Country[] getCountries() throws EmfException {
        EmfCall call = call();

        call.setOperation("getCountries");
        call.setReturnType(mappings.countries());

        return (Country[]) call.requestResponse(new Object[] {});
    }

    public Sector[] getSectors() throws EmfException {
        EmfCall call = call();

        call.setOperation("getSectors");
        call.setReturnType(mappings.sectors());

        return (Sector[]) call.requestResponse(new Object[] {});
    }

    public Keyword[] getKeywords() throws EmfException {
        EmfCall call = call();

        call.setOperation("getKeywords");
        call.setReturnType(mappings.keywords());

        return (Keyword[]) call.requestResponse(new Object[] {});
    }

    public Sector obtainLockedSector(User owner, Sector sector) throws EmfException {
        EmfCall call = call();

        call.addParam("owner", mappings.user());
        call.addParam("sector", mappings.sector());
        call.setOperation("obtainLockedSector");
        call.setReturnType(mappings.sector());

        return (Sector) call.requestResponse(new Object[] { owner, sector });
    }

    public Sector updateSector(Sector sector) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateSector");
        call.addParam("sector", mappings.sector());
        call.setReturnType(mappings.sector());

        return (Sector) call.requestResponse(new Object[] { sector });
    }

    public Sector releaseLockedSector(Sector sector) throws EmfException {
        EmfCall call = call();

        call.setOperation("releaseLockedSector");
        call.addParam("sector", mappings.sector());
        call.setReturnType(mappings.sector());

        return (Sector) call.requestResponse(new Object[] { sector });
    }

    public DatasetType[] getDatasetTypes() throws EmfException {
        EmfCall call = call();

        call.setOperation("getDatasetTypes");
        call.setReturnType(mappings.datasetTypes());

        return (DatasetType[]) call.requestResponse(new Object[] {});
    }

    public DatasetType obtainLockedDatasetType(User owner, DatasetType type) throws EmfException {
        EmfCall call = call();

        call.setOperation("obtainLockedDatasetType");
        call.addParam("owner", mappings.user());
        call.addParam("type", mappings.datasetType());
        call.setReturnType(mappings.datasetType());

        return (DatasetType) call.requestResponse(new Object[] { owner, type });
    }

    public DatasetType updateDatasetType(DatasetType type) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateDatasetType");
        call.addParam("type", mappings.datasetType());
        call.setReturnType(mappings.datasetType());

        return (DatasetType) call.requestResponse(new Object[] { type });
    }

    public DatasetType releaseLockedDatasetType(User owner, DatasetType type) throws EmfException {
        EmfCall call = call();

        call.setOperation("releaseLockedDatasetType");
        call.addParam("owner", mappings.user());
        call.addParam("type", mappings.datasetType());
        call.setReturnType(mappings.datasetType());

        return (DatasetType) call.requestResponse(new Object[] { owner, type });
    }

    public Status[] getStatuses(String username) throws EmfException {
        EmfCall call = call();

        call.setOperation("getStatuses");
        call.addStringParam("username");
        call.setReturnType(mappings.statuses());

        return (Status[]) call.requestResponse(new Object[] { username });
    }

    public Project[] getProjects() throws EmfException {
        EmfCall call = call();

        call.setOperation("getProjects");
        call.setReturnType(mappings.projects());

        return (Project[]) call.requestResponse(new Object[] {});
    }

    public Region[] getRegions() throws EmfException {
        EmfCall call = call();

        call.setOperation("getRegions");
        call.setReturnType(mappings.regions());

        return (Region[]) call.requestResponse(new Object[] {});
    }

    public IntendedUse[] getIntendedUses() throws EmfException {
        EmfCall call = call();

        call.setOperation("getIntendedUses");
        call.setReturnType(mappings.intendeduses());

        return (IntendedUse[]) call.requestResponse(new Object[] {});
    }

    public void addRegion(Region region) throws EmfException {
        EmfCall call = call();

        call.addParam("region", mappings.region());
        call.setOperation("addRegion");
        call.setVoidReturnType();

        call.request(new Object[] { region });
    }

    public void addProject(Project project) throws EmfException {
        EmfCall call = call();

        call.addParam("project", mappings.project());
        call.setOperation("addProject");
        call.setVoidReturnType();

        call.request(new Object[] { project });
    }

    public void addIntendedUse(IntendedUse intendedUse) throws EmfException {
        EmfCall call = call();

        call.addParam("intendeduse", mappings.intendeduse());
        call.setOperation("addIntendedUse");
        call.setVoidReturnType();

        call.request(new Object[] { intendedUse });
    }

    public void addSector(Sector sector) throws EmfException {
        EmfCall call = call();

        call.addParam("sector", mappings.sector());
        call.setOperation("addSector");
        call.setVoidReturnType();

        call.request(new Object[] { sector });
    }

    public void addDatasetType(DatasetType type) throws EmfException {
        EmfCall call = call();

        call.addParam("type", mappings.datasetType());
        call.setOperation("addDatasetType");
        call.setVoidReturnType();

        call.request(new Object[] { type });
    }

    public Note[] getNotes(long datasetId) throws EmfException {
        EmfCall call = call();

        call.addLongParam("datasetId");
        call.setOperation("getNotes");
        call.setReturnType(mappings.notes());

        return (Note[]) call.requestResponse(new Object[] { new Long(datasetId) });
    }

    public void addNote(Note note) throws EmfException{
        EmfCall call = call();

        call.addParam("note", mappings.note());
        call.setOperation("addNote");
        call.setVoidReturnType();

        call.request(new Object[] { note });
    }

    public void addNotes(Note[] notes) throws EmfException {
        EmfCall call = call();

        call.addParam("notes", mappings.notes());
        call.setOperation("addNotes");
        call.setVoidReturnType();

        call.request(new Object[] { notes });
    }

    public NoteType[] getNoteTypes() throws EmfException {
        EmfCall call = call();

        call.setOperation("getNoteTypes");
        call.setReturnType(mappings.notetypes());

        return (NoteType[]) call.requestResponse(new Object[] {});
    }

    public Revision[] getRevisions(long datasetId) throws EmfException{
        EmfCall call = call();

        call.addLongParam("datasetId");
        call.setOperation("getRevisions");
        call.setReturnType(mappings.revisions());

        return (Revision[]) call.requestResponse(new Object[] { new Long(datasetId) });
    }

    public void addRevision(Revision revision) throws EmfException{
        EmfCall call = call();

        call.addParam("revision", mappings.revision());
        call.setOperation("addRevision");
        call.setVoidReturnType();

        call.request(new Object[] { revision });
    }

}
