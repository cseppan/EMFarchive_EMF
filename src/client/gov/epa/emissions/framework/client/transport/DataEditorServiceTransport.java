package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataAccessToken;
import gov.epa.emissions.framework.services.DataEditorService;

public class DataEditorServiceTransport implements DataEditorService {
    private EmfMappings mappings;

    private EmfCall call;

    public DataEditorServiceTransport(EmfCall call) {
        this.call = call;
        mappings = new EmfMappings();
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

    public Page getPageWithRecord(DataAccessToken token, int recordId) throws EmfException {
        call.setOperation("getPageWithRecord");
        call.setReturnType(mappings.page());

        call.addParam("token", mappings.dataAccessToken());
        call.addIntegerParam("recordId");

        return (Page) call.requestResponse(new Object[] { token, new Integer(recordId) });
    }

    public int getTotalRecords(DataAccessToken token) throws EmfException {
        call.setOperation("getTotalRecords");
        call.addParam("token", mappings.dataAccessToken());
        call.setIntegerReturnType();

        Integer cnt = (Integer) call.requestResponse(new Object[] { token });
        return cnt.intValue();
    }

    public void close() throws EmfException {
        call.setOperation("close");
        call.setVoidReturnType();

        call.request(new Object[0]);
    }

    public Version derive(Version baseVersion, String name) throws EmfException {
        call.addParam("baseVersion", mappings.version());
        call.addStringParam("name");
        call.setOperation("derive");
        call.setReturnType(mappings.version());

        return (Version) call.requestResponse(new Object[] { baseVersion, name });
    }

    public void submit(DataAccessToken token, ChangeSet changeset, int pageNumber) throws EmfException {
        call.addParam("token", mappings.dataAccessToken());
        call.addParam("changeset", mappings.changeset());
        call.addIntegerParam("pageNumber");
        call.setOperation("submit");
        call.setVoidReturnType();

        call.request(new Object[] { token, changeset, new Integer(pageNumber) });
    }

    public boolean hasChanges(DataAccessToken token) throws EmfException {
        call.addParam("token", mappings.dataAccessToken());
        call.setOperation("hasChanges");
        call.setBooleanReturnType();

        Object result = call.requestResponse(new Object[] { token });
        return ((Boolean) result).booleanValue();
    }

    public void discard(DataAccessToken token) throws EmfException {
        call.addParam("token", mappings.dataAccessToken());
        call.setOperation("discard");
        call.setVoidReturnType();

        call.request(new Object[] { token });
    }

    public DataAccessToken openSession(User user, DataAccessToken token) throws EmfException {
        call.setOperation("openSession");
        call.addParam("user", mappings.user());
        call.addParam("token", mappings.dataAccessToken());
        call.setReturnType(mappings.dataAccessToken());

        return (DataAccessToken) call.requestResponse(new Object[] { user, token });
    }

    public void closeSession(DataAccessToken token) throws EmfException {
        call.addParam("token", mappings.dataAccessToken());
        call.setOperation("closeSession");
        call.setVoidReturnType();

        call.request(new Object[] { token });
    }

    public DataAccessToken save(DataAccessToken token) throws EmfException {
        call.addParam("token", mappings.dataAccessToken());
        call.setOperation("save");
        call.setReturnType(mappings.dataAccessToken());

        return (DataAccessToken) call.requestResponse(new Object[] { token });
    }

    public Version markFinal(DataAccessToken token) throws EmfException {
        call.addParam("token", mappings.dataAccessToken());
        call.setOperation("markFinal");
        call.setReturnType(mappings.version());

        return (Version) call.requestResponse(new Object[] { token });
    }

    public Version[] getVersions(long datasetId) throws EmfException {
        call.addLongParam("datasetId");
        call.setOperation("getVersions");
        call.setReturnType(mappings.versions());

        return (Version[]) call.requestResponse(new Object[] { new Long(datasetId) });
    }

    public Page applyConstraints(DataAccessToken token, String rowFilter, String sortOrder) throws EmfException {
        call.addParam("token", mappings.dataAccessToken());
        call.addStringParam("rowFilter");
        call.addStringParam("sortOrder");

        call.setOperation("applyConstraints");
        call.setReturnType(mappings.page());

        return (Page) call.requestResponse(new Object[] { token, rowFilter, sortOrder });
    }

}
