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

import org.apache.axis.AxisFault;
import org.apache.axis.client.Call;

public class DataCommonsServiceTransport implements DataCommonsService {
    private EmfMappings mappings;

    private CallFactory callFactory;

    public DataCommonsServiceTransport(String endPoint) {
        callFactory = new CallFactory(endPoint);
        mappings = new EmfMappings();
    }

    public Country[] getCountries() throws EmfException {
        try {
            Call call = call();

            mappings.register(call);
            mappings.setOperation(call, "getCountries");
            mappings.setReturnType(call, mappings.countries());

            return (Country[]) call.invoke(new Object[] {});
        } catch (AxisFault fault) {
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to DataCommons Service");
        }
    }

    public Sector[] getSectors() throws EmfException {
        try {
            Call call = call();

            mappings.register(call);
            mappings.setOperation(call, "getSectors");
            mappings.setReturnType(call, mappings.sectors());

            return (Sector[]) call.invoke(new Object[] {});
        } catch (AxisFault fault) {
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to DataCommons Service");
        }
    }

    public Keyword[] getKeywords() throws EmfException {
        try {
            Call call = call();

            mappings.register(call);
            mappings.setOperation(call, "getKeywords");
            mappings.setReturnType(call, mappings.keywords());

            return (Keyword[]) call.invoke(new Object[] {});
        } catch (AxisFault fault) {
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to DataCommons Service");
        }
    }

    public Sector obtainLockedSector(User owner, Sector sector) throws EmfException {
        try {
            Call call = call();

            mappings.register(call);
            mappings.setOperation(call, "obtainLockedSector");
            mappings.addParam(call, "owner", mappings.user());
            mappings.addParam(call, "sector", mappings.sector());
            mappings.setReturnType(call, mappings.sector());

            return (Sector) call.invoke(new Object[] { owner, sector });
        } catch (AxisFault fault) {
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to DataCommons Service");
        }
    }

    public Sector updateSector(Sector sector) throws EmfException {
        try {
            Call call = call();

            mappings.register(call);
            mappings.setOperation(call, "updateSector");
            mappings.addParam(call, "sector", mappings.sector());
            mappings.setReturnType(call, mappings.sector());

            return (Sector) call.invoke(new Object[] { sector });
        } catch (AxisFault fault) {
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to DataCommons Service");
        }
    }

    public Sector releaseLockedSector(Sector sector) throws EmfException {
        try {
            Call call = call();

            mappings.register(call);
            mappings.setOperation(call, "releaseLockedSector");
            mappings.addParam(call, "sector", mappings.sector());
            mappings.setReturnType(call, mappings.sector());

            return (Sector) call.invoke(new Object[] { sector });
        } catch (AxisFault fault) {
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to DataCommons Service");
        }
    }

    public DatasetType[] getDatasetTypes() throws EmfException {
        try {
            Call call = call();

            mappings.register(call);
            mappings.setOperation(call, "getDatasetTypes");
            mappings.setReturnType(call, mappings.datasetTypes());

            return (DatasetType[]) call.invoke(new Object[] {});
        } catch (AxisFault fault) {
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to DataCommons Service");
        }
    }

    public DatasetType obtainLockedDatasetType(User owner, DatasetType type) throws EmfException {
        try {
            Call call = call();

            mappings.register(call);
            mappings.setOperation(call, "obtainLockedDatasetType");
            mappings.addParam(call, "owner", mappings.user());
            mappings.addParam(call, "type", mappings.datasetType());
            mappings.setReturnType(call, mappings.datasetType());

            return (DatasetType) call.invoke(new Object[] { owner, type });
        } catch (AxisFault fault) {
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to DataCommons Service");
        }
    }

    public DatasetType updateDatasetType(DatasetType type) throws EmfException {
        try {
            Call call = call();

            mappings.register(call);
            mappings.setOperation(call, "updateDatasetType");
            mappings.addParam(call, "type", mappings.datasetType());
            mappings.setReturnType(call, mappings.datasetType());

            return (DatasetType) call.invoke(new Object[] { type });
        } catch (AxisFault fault) {
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to DataCommons Service");
        }
    }

    public DatasetType releaseLockedDatasetType(User owner, DatasetType type) throws EmfException {
        try {
            Call call = call();

            mappings.register(call);
            mappings.setOperation(call, "releaseLockedDatasetType");
            mappings.addParam(call, "owner", mappings.user());
            mappings.addParam(call, "type", mappings.datasetType());
            mappings.setReturnType(call, mappings.datasetType());

            return (DatasetType) call.invoke(new Object[] { owner, type });
        } catch (AxisFault fault) {
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to DataCommons Service");
        }
    }

    public Status[] getStatuses(String username) throws EmfException {
        try {
            Call call = call();
            call.setMaintainSession(true);

            mappings.register(call);
            mappings.setOperation(call, "getStatuses");
            mappings.addStringParam(call, "username");
            mappings.setReturnType(call, mappings.statuses());

            return (Status[]) call.invoke(new Object[] { username });
        } catch (AxisFault fault) {
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to DataCommons Service");
        }
    }

    public Project[] getProjects() throws EmfException {
        try {
            Call call = call();

            mappings.register(call);
            mappings.setOperation(call, "getProjects");
            mappings.setReturnType(call, mappings.projects());

            return (Project[]) call.invoke(new Object[] {});
        } catch (AxisFault fault) {
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to DataCommons Service");
        }
    }

    public Region[] getRegions() throws EmfException {
        try {
            Call call = call();

            mappings.register(call);
            mappings.setOperation(call, "getRegions");
            mappings.setReturnType(call, mappings.regions());

            return (Region[]) call.invoke(new Object[] {});
        } catch (AxisFault fault) {
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to DataCommons Service");
        }
    }

    public IntendedUse[] getIntendedUses() throws EmfException {
        try {
            Call call = call();

            mappings.register(call);
            mappings.setOperation(call, "getIntendedUses");
            mappings.setReturnType(call, mappings.intendeduses());

            return (IntendedUse[]) call.invoke(new Object[] {});
        } catch (AxisFault fault) {
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to DataCommons Service");
        }
    }

    public void addRegion(Region region) throws EmfException {
        Call call = call();

        mappings.addParam(call, "region", mappings.region());
        mappings.setOperation(call, "addRegion");
        mappings.setVoidReturnType(call);
        Object[] params = new Object[] { region };

        invoke(call, params);
    }

    private void invoke(Call call, Object[] params) throws EmfException {
        try {
            call.invoke(params);
        } catch (AxisFault fault) {
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to DataCommons Service");
        }
    }

    private Call call() throws EmfException {
        try {
            Call call = callFactory.createCall();
            mappings.register(call);
            return call;
        } catch (Exception e) {
            throw new EmfException("Unable to connect to DataCommons Service");
        }
    }

    public void addProject(Project project) throws EmfException {
        try {
            Call call = call();

            mappings.register(call);
            mappings.addParam(call, "project", mappings.project());
            mappings.setOperation(call, "addProject");
            mappings.setVoidReturnType(call);
            call.invoke(new Object[] { project });

        } catch (AxisFault fault) {
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to DataCommons Service");
        }
    }

    public void addIntendedUse(IntendedUse intendedUse) throws EmfException {
        try {
            Call call = call();

            mappings.register(call);
            mappings.addParam(call, "intendeduse", mappings.intendeduse());
            mappings.setOperation(call, "addIntendedUse");
            mappings.setVoidReturnType(call);
            call.invoke(new Object[] { intendedUse });

        } catch (AxisFault fault) {
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to DataCommons Service");
        }
    }

    public void addSector(Sector sector) throws EmfException {
        try {
            Call call = call();

            mappings.register(call);
            mappings.addParam(call, "sector", mappings.sector());
            mappings.setOperation(call, "addSector");
            mappings.setVoidReturnType(call);
            call.invoke(new Object[] { sector });

        } catch (AxisFault fault) {
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to DataCommons Service");
        }
    }

    public void addDatasetType(DatasetType type) throws EmfException {
        try {
            Call call = call();

            mappings.register(call);
            mappings.addParam(call, "type", mappings.datasetType());
            mappings.setOperation(call, "addDatasetType");
            mappings.setVoidReturnType(call);
            call.invoke(new Object[] { type });

        } catch (AxisFault fault) {
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to DataCommons Service");
        }
    }
}
