package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;

public interface DataEditorService extends DataAccessService {
    // edit
    void submit(DataAccessToken token, ChangeSet changeset, int page) throws EmfException;

    void discard(DataAccessToken token) throws EmfException;

    DataAccessToken save(DataAccessToken token) throws EmfException;

    Version derive(Version baseVersion, String name) throws EmfException;

    Version markFinal(DataAccessToken token) throws EmfException;

    boolean hasChanges(DataAccessToken token) throws EmfException;

    // session
    DataAccessToken openSession(User user, DataAccessToken token) throws EmfException;

    void closeSession(DataAccessToken token) throws EmfException;
    
}