package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.notes.NewNoteView;
import gov.epa.emissions.framework.services.DataAccessToken;
import gov.epa.emissions.framework.services.DataCommonsService;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.Note;
import gov.epa.emissions.framework.services.NoteType;
import gov.epa.emissions.framework.services.Revision;

import java.util.Date;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.Constraint;
import org.jmock.core.constraint.HasPropertyWithValue;
import org.jmock.core.constraint.IsInstanceOf;

public class DataEditorPresenterTest extends MockObjectTestCase {

    public void testShouldLoadTablesOfDatasetOnDisplay() throws Exception {
        Version version = new Version();
        String table = "table";

        Mock service = mock(DataEditorService.class);
        Constraint constraint = tokenConstraint(version, table);
        DataAccessToken token = successToken();
        User user = new User();
        service.expects(once()).method("openSession").with(same(user), constraint).will(returnValue(token));

        DataEditorService serviceProxy = (DataEditorService) service.proxy();

        Mock view = mock(DataEditorView.class);
        view.expects(once()).method("display").with(eq(version), eq(table), same(user), same(serviceProxy));
        view.expects(once()).method("updateLockPeriod")
                .with(new IsInstanceOf(Date.class), new IsInstanceOf(Date.class));

        EmfSession session = session(user, serviceProxy, null);
        DataEditorPresenter p = new DataEditorPresenter(null, version, table, session);
        view.expects(once()).method("observe").with(same(p));

        p.display((DataEditorView) view.proxy());
    }

    private EmfSession session(User user, DataEditorService editor, DataCommonsService commons) {
        Mock session = mock(EmfSession.class);
        session.stubs().method("dataEditorService").will(returnValue(editor));
        session.stubs().method("dataCommonsService").will(returnValue(commons));
        session.stubs().method("user").will(returnValue(user));

        return (EmfSession) session.proxy();
    }

    public void testShouldAbortWithNotificationIfUnableToObtainLockOnDisplay() throws Exception {
        Version version = new Version();
        String table = "table";

        Mock service = mock(DataEditorService.class);
        DataAccessToken failureToken = failureToken();
        User user = new User();
        service.expects(once()).method("openSession").with(same(user), new IsInstanceOf(DataAccessToken.class)).will(
                returnValue(failureToken));

        DataEditorService serviceProxy = (DataEditorService) service.proxy();

        Mock view = mock(DataEditorView.class);
        view.expects(once()).method("notifyLockFailure");

        EmfSession session = session(user, serviceProxy, null);
        DataEditorPresenter p = new DataEditorPresenter(null, version, table, session);

        p.display((DataEditorView) view.proxy());
    }

    private DataAccessToken successToken() {
        Mock mock = mock(DataAccessToken.class);
        mock.stubs().method("isLocked").will(returnValue(Boolean.TRUE));
        mock.stubs().method("lockStart").will(returnValue(new Date()));
        mock.stubs().method("lockEnd").will(returnValue(new Date()));

        return (DataAccessToken) mock.proxy();
    }

    private DataAccessToken failureToken() {
        Mock mock = mock(DataAccessToken.class);
        mock.stubs().method("isLocked").will(returnValue(Boolean.FALSE));

        return (DataAccessToken) mock.proxy();
    }

    public void testShouldCloseViewAndCloseDataEditSessionOnClose() throws Exception {
        Mock closingRule = mock(ClosingRule.class);
        closingRule.expects(once()).method("close");

        Mock service = mock(DataCommonsService.class);

        Revision revision = new Revision();
        service.expects(once()).method("addRevision").with(same(revision));

        DataCommonsService serviceProxy = (DataCommonsService) service.proxy();
        EmfSession session = session(null, null, serviceProxy);

        DataEditorPresenter p = new DataEditorPresenter(null, null, null, session);
        p.close((ClosingRule) closingRule.proxy(), serviceProxy, revision);
    }

    public void testShouldDiscardChangesOnDiscard() throws Exception {
        Mock service = mock(DataEditorService.class);
        DataAccessToken token = new DataAccessToken();
        service.expects(once()).method("discard").with(same(token));

        DataEditorPresenter p = new DataEditorPresenter(null, null, null, null);
        p.discard((DataEditorService) service.proxy(), token);
    }

    public void testShouldDisplayTableViewOnDisplayTableView() throws Exception {
        DataEditorPresenter p = new DataEditorPresenter(null, null, null, null);

        Mock tablePresenter = mock(EditableTablePresenter.class);
        tablePresenter.expects(once()).method("observe");
        tablePresenter.expects(once()).method("doDisplayFirst");

        p.displayTable((EditableTablePresenter) tablePresenter.proxy());
    }

    public void testShouldSubmitAnyChangesAndSaveChangesOnSave() throws Exception {
        Mock view = mock(DataEditorView.class);
        view.expects(once()).method("updateLockPeriod");

        Mock service = mock(DataEditorService.class);
        DataAccessToken token = new DataAccessToken();
        Version version = new Version();
        token.setVersion(version);
        service.expects(once()).method("save").with(same(token)).will(returnValue(token));
        DataEditorService serviceProxy = (DataEditorService) service.proxy();

        DataEditorView viewProxy = (DataEditorView) view.proxy();

        Mock tablePresenter = mock(EditableTablePresenter.class);
        tablePresenter.expects(once()).method("submitChanges");
        EditableTablePresenter tablePresenterProxy = (EditableTablePresenter) tablePresenter.proxy();

        DataEditorPresenter p = new DataEditorPresenter(null, version, null, null);
        p.save(viewProxy, token, tablePresenterProxy, serviceProxy, null);
    }

    public void testOnSaveShouldDiscardChangesCloseSessionAndNotifyUserOfFailureIfSaveFails() throws Exception {
        Mock view = mock(DataEditorView.class);
        view.expects(once()).method("notifySaveFailure").with(eq("Failure"));

        Mock service = mock(DataEditorService.class);
        service.expects(once()).method("save").with(new IsInstanceOf(DataAccessToken.class)).will(
                throwException(new EmfException("Failure")));

        DataAccessToken token = new DataAccessToken();
        service.expects(once()).method("discard").with(same(token));

        DataEditorView viewProxy = (DataEditorView) view.proxy();

        Mock tablePresenter = mock(EditableTablePresenter.class);
        tablePresenter.expects(once()).method("submitChanges");
        EditableTablePresenter tablePresenterProxy = (EditableTablePresenter) tablePresenter.proxy();

        DataEditorService serviceProxy = (DataEditorService) service.proxy();

        Mock closingRule = mock(ClosingRule.class);
        closingRule.expects(once()).method("proceedWithClose");
        ClosingRule closingRuleProxy = (ClosingRule) closingRule.proxy();

        DataEditorPresenter p = new DataEditorPresenter(null, null, null, null);
        p.save(viewProxy, token, tablePresenterProxy, serviceProxy, closingRuleProxy);
    }

    private Constraint tokenConstraint(Version version, String table) {
        Constraint propertyConstraint = and(new HasPropertyWithValue("version", same(version)),
                new HasPropertyWithValue("table", eq(table)));
        Constraint constraint = and(new IsInstanceOf(DataAccessToken.class), propertyConstraint);
        return constraint;
    }

    public void testShouldRunNewNoteViewAndAddNoteOnAddNote() throws Exception {
        Note note = new Note();
        NoteType[] types = new NoteType[0];
        Version[] versions = new Version[0];
        User user = new User();

        Mock service = mock(DataCommonsService.class);
        service.expects(once()).method("addNote").with(same(note));

        EmfSession session = session(user, null, (DataCommonsService) service.proxy());
        DataEditorPresenter presenter = new DataEditorPresenter(null, null, null, session);

        Mock view = mock(NewNoteView.class);
        EmfDataset dataset = new EmfDataset();
        view.stubs().method("display").with(same(user), same(dataset), same(types), same(versions));
        view.stubs().method("shouldCreate").will(returnValue(Boolean.TRUE));
        view.stubs().method("note").will(returnValue(note));

        presenter.addNote((NewNoteView) view.proxy(), user, dataset, types, versions);
    }

    public void testShouldSaveRevisionOnClose() throws Exception {
        Mock service = mock(DataCommonsService.class);

        Revision revision = new Revision();
        service.expects(once()).method("addRevision").with(same(revision));

        DataCommonsService serviceProxy = (DataCommonsService) service.proxy();
        EmfSession session = session(null, null, serviceProxy);
        DataEditorPresenter presenter = new DataEditorPresenter(null, null, null, session);

        Mock closingRule = mock(ClosingRule.class);
        closingRule.expects(once()).method("close");

        presenter.close((ClosingRule) closingRule.proxy(), serviceProxy, revision);
    }

}
