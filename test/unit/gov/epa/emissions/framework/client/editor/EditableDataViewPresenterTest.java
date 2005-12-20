package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.VersionedRecord;
import gov.epa.emissions.framework.EmfException;
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
        service.expects(once()).method("openSession").with(constraint);

        DataEditorService serviceProxy = (DataEditorService) service.proxy();

        Mock view = mock(EditableDataView.class);
        view.expects(once()).method("display").with(eq(version), eq(table), same(serviceProxy));

        EditableDataViewPresenter p = new EditableDataViewPresenter(version, table, (EditableDataView) view.proxy(),
                serviceProxy);
        view.expects(once()).method("observe").with(same(p));

        p.display();
    }

    public void testShouldCloseViewAndCloseDataEditSessionOnClose() throws Exception {
        Mock view = mock(EditableDataView.class);
        view.expects(once()).method("close").withNoArguments();

        Version version = new Version();
        String table = "table";

        Mock service = mock(DataEditorService.class);
        Constraint constraint = tokenConstraint(version, table);
        service.expects(once()).method("closeSession").with(constraint);

        EditableDataViewPresenter p = new EditableDataViewPresenter(version, table, (EditableDataView) view.proxy(),
                (DataEditorService) service.proxy());

        p.doClose();
    }

    public void testShouldDiscardChangesOnDiscard() throws Exception {
        Version version = new Version();
        String table = "table";

        Mock services = mock(DataEditorService.class);
        Constraint constraint = tokenConstraint(version, table);
        services.expects(once()).method("discard").with(constraint);

        EditableDataViewPresenter p = new EditableDataViewPresenter(version, table, null, (DataEditorService) services
                .proxy());

        p.doDiscard();
    }

    public void testShouldSaveChangesOnSave() throws Exception {
        Version version = new Version();
        String table = "table";

        Mock service = mock(DataEditorService.class);
        Constraint constraint = tokenConstraint(version, table);
        service.expects(once()).method("save").with(constraint);

        EditableDataViewPresenter p = new EditableDataViewPresenter(version, table, null, (DataEditorService) service
                .proxy());

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

        EditableDataViewPresenter p = new EditableDataViewPresenter(version, table, null, (DataEditorService) service
                .proxy());

        p.displayTable((EditableTableView) tableView.proxy());
    }

    public void testShouldSubmitAnyChangesAndSaveChangesOnSave() throws Exception {
        Version version = new Version();
        String table = "table";

        Mock service = mock(DataEditorService.class);
        Constraint constraint = tokenConstraint(version, table);
        service.expects(once()).method("save").with(constraint);

        EditableDataViewPresenter p = new EditableDataViewPresenter(version, table, null, (DataEditorService) service
                .proxy());

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
