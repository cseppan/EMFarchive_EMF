package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.io.Country;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.commons.io.Project;
import gov.epa.emissions.commons.io.Region;
import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataCommonsService;
import gov.epa.emissions.framework.services.IntendedUse;
import gov.epa.emissions.framework.services.Status;

public class DataCommonsServiceTransport implements DataCommonsService {
    private EmfMappings mappings;

    private CallFactory callFactory;

    public DataCommonsServiceTransport(String endPoint) {
        callFactory = new CallFactory(endPoint);
        mappings = new EmfMappings();
    }

    private EmfCall call() throws EmfException {
        return callFactory.createEmfCall("DataCommons Service");
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
}
