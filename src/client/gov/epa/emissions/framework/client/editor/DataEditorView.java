package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.DataAccessToken;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.Revision;

import java.util.Date;

public interface DataEditorView extends ManagedView {
    
    void display(Version version, String table, User user, DataEditorService service) throws EmfException;

    void observe(DataEditorPresenter presenter);

    void notifyLockFailure(DataAccessToken token);

    void updateLockPeriod(Date start, Date end);

    void notifySaveFailure(String message);

    boolean confirmDiscardChanges();

    Revision revision();

    boolean verifyRevisionInput();
}
