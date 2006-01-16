package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.VersionedRecord;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.EditToken;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.Constraint;
import org.jmock.core.constraint.HasPropertyWithValue;
import org.jmock.core.constraint.IsInstanceOf;

public class EditableDataViewPresenterTest extends MockObjectTestCase {

    public void testShouldLoadTablesOfDatasetOnDisplay() throws Exception {
        Version version = new Version();
        String table = "table";

        Mock service = mock(DataEditorService.class);
        Constraint constraint = tokenConstraint(version, table);
        EditToken token = token();
        service.expects(once()).method("openSession").with(constraint).will(returnValue(token));

        DataEditorService serviceProxy = (DataEditorService) service.proxy();

        Mock view = mock(EditableDataView.class);
        view.expects(once()).method("display").with(eq(version), eq(table), same(serviceProxy));

        Mock session = mock(EmfSession.class);
        session.stubs().method("user").withNoArguments().will(returnValue(null));

        EditableDataViewPresenter p = new EditableDataViewPresenter((EmfSession) session.proxy(), version, table,
                (EditableDataView) view.proxy(), serviceProxy);
        view.expects(once()).method("observe").with(same(p));

        p.display();
    }

    private EditToken token() {
        Mock mock = mock(EditToken.class);
        mock.stubs().method("isLocked").will(returnValue(Boolean.TRUE));

        return (EditToken) mock.proxy();
    }

    public void testShouldCloseViewAndCloseDataEditSessionOnClose() throws Exception {
        Mock view = mock(EditableDataView.class);
        view.expects(once()).method("close").withNoArguments();

        Mock service = mock(DataEditorService.class);
        service.expects(once()).method("closeSession").with(new IsInstanceOf(EditToken.class));

        EditableDataViewPresenter p = displayPresenter(view, service);

        p.doClose();
    }

    private EditableDataViewPresenter displayPresenter(Mock view, Mock service) throws EmfException {
        Version version = new Version();
        String table = "table";

        EditToken token = token();
        service.expects(once()).method("openSession").with(new IsInstanceOf(EditToken.class)).will(returnValue(token));

        Mock session = mock(EmfSession.class);
        session.stubs().method("user").withNoArguments().will(returnValue(null));

        DataEditorService serviceProxy = (DataEditorService) service.proxy();
        view.expects(once()).method("display").with(eq(version), eq(table), same(serviceProxy));

        EditableDataViewPresenter p = new EditableDataViewPresenter((EmfSession) session.proxy(), version, table,
                (EditableDataView) view.proxy(), serviceProxy);
        view.expects(once()).method("observe").with(same(p));
        p.display();

        return p;
    }

    public void testShouldDiscardChangesOnDiscard() throws Exception {
        Mock view = mock(EditableDataView.class);

        Mock service = mock(DataEditorService.class);
        service.expects(once()).method("discard").with(new IsInstanceOf(EditToken.class));

        EditableDataViewPresenter p = displayPresenter(view, service);

        p.doDiscard();
    }

    public void testShouldSaveChangesOnSave() throws Exception {
        Mock view = mock(EditableDataView.class);

        Mock service = mock(DataEditorService.class);
        service.expects(once()).method("save").with(new IsInstanceOf(EditToken.class));

        EditableDataViewPresenter p = displayPresenter(view, service);

        Mock tableView = displayTableView(service, p);
        ChangeSet changeset = new ChangeSet();
        tableView.stubs().method("changeset").withNoArguments().will(returnValue(changeset));

        p.doSave();
    }

    public void testShouldDisplayTableViewOnDisplayTableView() throws Exception {
        Mock tableView = mock(EditableTableView.class);
        tableView.expects(once()).method("observe").with(new IsInstanceOf(EditableTablePresenter.class));
        tableView.expects(once()).method("display").with(new IsInstanceOf(Page.class));
        tableView.stubs().method("changeset").withNoArguments().will(returnValue(new ChangeSet()));

        Version version = new Version();
        String table = "table";

        Mock service = mock(DataEditorService.class);
        service.stubs().method("getPage").withAnyArguments().will(returnValue(new Page()));

        EditableDataViewPresenter p = new EditableDataViewPresenter(null, version, table, null,
                (DataEditorService) service.proxy());

        p.displayTable((EditableTableView) tableView.proxy());
    }

    public void testShouldSubmitAnyChangesAndSaveChangesOnSave() throws Exception {
        Mock view = mock(EditableDataView.class);

        Mock service = mock(DataEditorService.class);
        service.expects(once()).method("save").with(new IsInstanceOf(EditToken.class));

        EditableDataViewPresenter p = displayPresenter(view, service);

        Mock tableView = displayTableView(service, p);

        ChangeSet changeset = new ChangeSet();
        changeset.addDeleted(new VersionedRecord());
        tableView.stubs().method("changeset").withNoArguments().will(returnValue(changeset));
        service.expects(once()).method("submit").with(new IsInstanceOf(EditToken.class), same(changeset), ANYTHING);

        p.doSave();
    }

    private Mock displayTableView(Mock service, EditableDataViewPresenter p) throws EmfException {
        Mock tableView = mock(EditableTableView.class);
        tableView.expects(once()).method("observe").with(new IsInstanceOf(EditableTablePresenter.class));
        tableView.expects(once()).method("display").with(new IsInstanceOf(Page.class));
        tableView.stubs().method("changeset").withNoArguments().will(returnValue(new ChangeSet()));
        service.stubs().method("getPage").withAnyArguments().will(returnValue(new Page()));

        p.displayTable((EditableTableView) tableView.proxy());
        return tableView;
    }

    private Constraint tokenConstraint(Version version, String table) {
        Constraint propertyConstraint = and(new HasPropertyWithValue("version", same(version)),
                new HasPropertyWithValue("table", eq(table)));
        Constraint constraint = and(new IsInstanceOf(EditToken.class), propertyConstraint);
        return constraint;
    }

}
