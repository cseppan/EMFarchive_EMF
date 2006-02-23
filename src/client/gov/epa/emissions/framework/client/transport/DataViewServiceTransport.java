package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataAccessToken;
import gov.epa.emissions.framework.services.DataViewService;

public class DataViewServiceTransport implements DataViewService {
    private EmfCall call;

    private EmfMappings mappings;

    public DataViewServiceTransport(EmfCall call) {
        this.call = call;
        mappings = new EmfMappings();
    }

    public Page applyConstraints(DataAccessToken token, String rowFilter, String sortOrder) throws EmfException {
        call.addParam("token", mappings.dataAccessToken());
        call.addStringParam("rowFilter");
        call.addStringParam("sortOrder");

        call.setOperation("applyConstraints");
        call.setReturnType(mappings.page());

        return (Page) call.requestResponse(new Object[] { token, rowFilter, sortOrder });
    }

    public Page getPage(DataAccessToken token, int pageNumber) throws EmfException {
        call.addParam("token", mappings.dataAccessToken());
        call.addIntegerParam("pageNumber");

        call.setOperation("getPage");
        call.setReturnType(mappings.page());

        return (Page) call.requestResponse(new Object[] { token, new Integer(pageNumber) });
    }

    public int getPageCount(DataAccessToken token) throws EmfException {
        call.setOperation("getPageCount");
        call.addParam("token", mappings.dataAccessToken());
        call.setIntegerReturnType();

        Integer cnt = (Integer) call.requestResponse(new Object[] { token });

        return cnt.intValue();
    }

    public Page getPageWithRecord(DataAccessToken token, int record) throws EmfException {
        call.setOperation("getPageWithRecord");
        call.setReturnType(mappings.page());

        call.addParam("token", mappings.dataAccessToken());
        call.addIntegerParam("record");

        return (Page) call.requestResponse(new Object[] { token, new Integer(record) });
    }

    public int getTotalRecords(DataAccessToken token) throws EmfException {
        call.setOperation("getTotalRecords");
        call.addParam("token", mappings.dataAccessToken());
        call.setIntegerReturnType();

        Integer cnt = (Integer) call.requestResponse(new Object[] { token });
        return cnt.intValue();
    }

    public DataAccessToken openSession(DataAccessToken token) throws EmfException {
        call.addParam("token", mappings.dataAccessToken());
        call.setOperation("openSession");
        call.setReturnType(mappings.dataAccessToken());

        return (DataAccessToken) call.requestResponse(new Object[] { token });
    }

    public void closeSession(DataAccessToken token) throws EmfException {
        call.addParam("token", mappings.dataAccessToken());
        call.setOperation("closeSession");
        call.setVoidReturnType();

        call.request(new Object[] { token });
    }

    public Version[] getVersions(long datasetId) throws EmfException {
        call.addLongParam("datasetId");
        call.setOperation("getVersions");
        call.setReturnType(mappings.versions());

        return (Version[]) call.requestResponse(new Object[] { new Long(datasetId) });
    }

    public TableMetadata getTableMetadata(String table) throws EmfException {
        call.addStringParam("table");

        call.setOperation("getTableMetadata");
        call.setReturnType(mappings.tablemetadata());
        
        return (TableMetadata)call.requestResponse(new Object[] { table });
    }
    
    

}
