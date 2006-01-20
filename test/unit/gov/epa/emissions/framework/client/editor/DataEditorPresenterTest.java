package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.VersionedRecord;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataAccessToken;
import gov.epa.emissions.framework.services.DataEditorService;

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
        view.expects(once()).method("display").with(eq(version), eq(table), same(serviceProxy));
        view.expects(once()).method("updateLockPeriod")
                .with(new IsInstanceOf(Date.class), new IsInstanceOf(Date.class));

        DataEditorPresenter p = new DataEditorPresenter(user, version, table, serviceProxy);
        view.expects(once()).method("observe").with(same(p));

        p.display((DataEditorView) view.proxy());
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

        DataEditorPresenter p = new DataEditorPresenter(user, version, table, serviceProxy);

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
        Mock view = mock(DataEditorView.class);
        view.expects(once()).method("close").withNoArguments();

        Mock service = mock(DataEditorService.class);
        service.expects(once()).method("closeSession").with(new IsInstanceOf(DataAccessToken.class));

        DataEditorPresenter p = displayPresenter(view, service);

        p.doClose();
    }

    private DataEditorPresenter displayPresenter(Mock view, Mock service) throws EmfException {
        Version version = new Version();
        String table = "table";

        DataAccessToken token = successToken();
        User user = new User();
        service.expects(once()).method("openSession").with(same(user), new IsInstanceOf(DataAccessToken.class)).will(
                returnValue(token));

        DataEditorService serviceProxy = (DataEditorService) service.proxy();
        view.expects(once()).method("display").with(eq(version), eq(table), same(serviceProxy));
        view.expects(once()).method("updateLockPeriod")
                .with(new IsInstanceOf(Date.class), new IsInstanceOf(Date.class));

        DataEditorPresenter p = new DataEditorPresenter(user, version, table, serviceProxy);
        view.expects(once()).method("observe").with(same(p));
        p.display((DataEditorView) view.proxy());

        return p;
    }

    public void testShouldDiscardChangesOnDiscard() throws Exception {
        Mock view = mock(DataEditorView.class);

        Mock service = mock(DataEditorService.class);
        service.expects(once()).method("discard").with(new IsInstanceOf(DataAccessToken.class));

        DataEditorPresenter p = displayPresenter(view, service);

        p.doDiscard();
    }

    public void testShouldDisplayTableViewOnDisplayTableView() throws Exception {
        Mock tableView = mock(EditablePageManagerView.class);
        tableView.expects(once()).method("observe").with(new IsInstanceOf(EditableTablePresenter.class));
        tableView.expects(once()).method("display").with(new IsInstanceOf(Page.class));
        tableView.stubs().method("changeset").withNoArguments().will(returnValue(new ChangeSet()));

        Version version = new Version();
        String table = "table";

        Mock service = mock(DataEditorService.class);
        service.stubs().method("getPage").withAnyArguments().will(returnValue(new Page()));

        DataEditorPresenter p = new DataEditorPresenter(null, version, table, (DataEditorService) service.proxy());

        p.displayTable((EditablePageManagerView) tableView.proxy());
    }

    public void testShouldSubmitAnyChangesAndSaveChangesOnSave() throws Exception {
        Mock view = mock(DataEditorView.class);

        Mock service = mock(DataEditorService.class);
        service.expects(once()).method("save").with(new IsInstanceOf(DataAccessToken.class));

        DataEditorPresenter p = displayPresenter(view, service);

        Mock tableView = displayTableView(service, p);

        ChangeSet changeset = new ChangeSet();
        changeset.addDeleted(new VersionedRecord());
        tableView.stubs().method("changeset").withNoArguments().will(returnValue(changeset));
        service.expects(once()).method("submit").with(new IsInstanceOf(DataAccessToken.class), same(changeset),
                ANYTHING);

        p.doSave();
    }

    public void testOnSaveShouldDiscardChangesCloseSessionAndNotifyUserOfFailureIfLockWasOwnedByAnotherUser()
            throws Exception {
        Mock view = mock(DataEditorView.class);
        view.expects(once()).method("notifySaveFailure").with(eq("Failure"));
        view.expects(once()).method("close").withNoArguments();

        Mock service = mock(DataEditorService.class);
        service.expects(once()).method("save").with(new IsInstanceOf(DataAccessToken.class)).will(
                throwException(new EmfException("Failure")));
        service.expects(once()).method("closeSession").with(new IsInstanceOf(DataAccessToken.class));

        DataEditorPresenter p = displayPresenter(view, service);
        Mock tableView = displayTableView(service, p);

        ChangeSet changeset = new ChangeSet();
        changeset.addDeleted(new VersionedRecord());
        tableView.stubs().method("changeset").withNoArguments().will(returnValue(changeset));
        service.expects(once()).method("submit").with(new IsInstanceOf(DataAccessToken.class), same(changeset),
                ANYTHING);

        service.expects(once()).method("discard").with(new IsInstanceOf(DataAccessToken.class));

        p.doSave();
    }

    private Mock displayTableView(Mock service, DataEditorPresenter p) throws EmfException {
        Mock tableView = mock(EditablePageManagerView.class);
        tableView.expects(once()).method("observe").with(new IsInstanceOf(EditableTablePresenter.class));
        tableView.expects(once()).method("display").with(new IsInstanceOf(Page.class));
        tableView.stubs().method("changeset").withNoArguments().will(returnValue(new ChangeSet()));
        service.stubs().method("getPage").withAnyArguments().will(returnValue(new Page()));

        p.displayTable((EditablePageManagerView) tableView.proxy());
        return tableView;
    }

    private Constraint tokenConstraint(Version version, String table) {
        Constraint propertyConstraint = and(new HasPropertyWithValue("version", same(version)),
                new HasPropertyWithValue("table", eq(table)));
        Constraint constraint = and(new IsInstanceOf(DataAccessToken.class), propertyConstraint);
        return constraint;
    }

}
