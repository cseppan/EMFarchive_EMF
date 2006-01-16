package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.editor.EditableDataView;
import gov.epa.emissions.framework.client.editor.EditableDataViewPresenter;
import gov.epa.emissions.framework.client.editor.NonEditableDataView;
import gov.epa.emissions.framework.client.editor.NonEditableDataViewPresenter;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.EmfDataset;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.constraint.IsInstanceOf;

public class EditVersionsPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayTableViewOnView() throws Exception {
        Version version = new Version();
        String table = "table";

        Mock service = mock(DataEditorService.class);
        service.expects(once()).method("openSession").withAnyArguments();
        DataEditorService serviceProxy = (DataEditorService) service.proxy();

        Mock dataView = mock(NonEditableDataView.class);
        dataView.expects(once()).method("display").with(same(version), eq(table), same(serviceProxy));
        dataView.expects(once()).method("observe").with(new IsInstanceOf(NonEditableDataViewPresenter.class));

        EditVersionsPresenter presenter = new EditVersionsPresenter(null, null, serviceProxy);
        presenter.doView(version, table, (NonEditableDataView) dataView.proxy());
    }

    public void testShouldDisplayEditableTableViewOnEdit() throws Exception {
        Version version = new Version();
        String table = "table";

        Mock service = mock(DataEditorService.class);
        service.expects(once()).method("openSession").withAnyArguments();
        DataEditorService serviceProxy = (DataEditorService) service.proxy();

        Mock dataView = mock(EditableDataView.class);
        dataView.expects(once()).method("display").with(same(version), eq(table), same(serviceProxy));
        dataView.expects(once()).method("observe").with(new IsInstanceOf(EditableDataViewPresenter.class));

        Mock session = mock(EmfSession.class);
        session.stubs().method("user").withNoArguments().will(returnValue(null));

        EditVersionsPresenter presenter = new EditVersionsPresenter(null, (EmfSession) session.proxy(), serviceProxy);
        presenter.doEdit(version, table, (EditableDataView) dataView.proxy());
    }

    public void testShouldRaiseErrorOnEditWhenVersionIsFinal() throws Exception {
        Version version = new Version();
        version.markFinal();

        EditVersionsPresenter presenter = new EditVersionsPresenter(null, null, null);

        try {
            presenter.doEdit(version, null, null);
        } catch (EmfException e) {
            assertEquals("Cannot edit a Version(" + version.getVersion() + ") that is Final.", e.getMessage());
            return;
        }

        fail("Should have failed to edit a Version that is already Final.");
    }

    public void testShouldDeriveNewVersionOnNew() throws Exception {
        Version version = new Version();
        Version derived = new Version();
        String derivedName = "name";

        Mock service = mock(DataEditorService.class);
        service.expects(once()).method("derive").with(same(version), eq(derivedName)).will(returnValue(derived));

        Mock view = mock(EditVersionsView.class);
        view.expects(once()).method("add").with(same(derived));

        EditVersionsPresenter presenter = displayPresenter(service, view);

        presenter.doNew(version, derivedName);
    }

    private EditVersionsPresenter displayPresenter(Mock service, Mock view) throws EmfException {
        EmfDataset dataset = new EmfDataset();
        dataset.setDatasetid(1);
        Version[] versions = new Version[0];
        InternalSource[] internalSources = new InternalSource[0];

        service.stubs().method("getVersions").with(eq(new Long(dataset.getDatasetid()))).will(returnValue(versions));

        EditVersionsPresenter presenter = new EditVersionsPresenter(dataset, null, (DataEditorService) service.proxy());
        view.expects(once()).method("observe").with(same(presenter));
        view.expects(once()).method("display").with(eq(versions), eq(internalSources));

        presenter.display((EditVersionsView) view.proxy());

        return presenter;
    }

    public void testShouldObserveAndDisplayViewOnDisplay() throws Exception {
        Mock service = mock(DataEditorService.class);
        Mock view = mock(EditVersionsView.class);

        displayPresenter(service, view);
    }

    public void testShouldMarkVersionAsFinalOnMarkFinal() throws Exception {
        Version version = new Version();
        version.setVersion(8);

        Mock service = mock(DataEditorService.class);
        service.expects(once()).method("markFinal").with(same(version)).will(returnValue(new Version()));

        Version[] versions = {};

        Mock view = mock(EditVersionsView.class);
        view.expects(once()).method("reload").with(eq(versions));

        EditVersionsPresenter p = displayPresenter(service, view);

        p.doMarkFinal(new Version[] { version });
    }

    public void testShouldRaiseErrorOnMarkFinalWhenVersionIsAlreadyFinal() throws Exception {
        Version version = new Version();
        version.setVersion(2);
        version.markFinal();

        EditVersionsPresenter p = new EditVersionsPresenter(null, null, null);

        try {
            p.doMarkFinal(new Version[] { version });
        } catch (EmfException e) {
            assertEquals("Version: " + version.getVersion() + " is already Final. It should be non-final.", e
                    .getMessage());
            return;
        }

        fail("Should have failed to mark Final when Version is already Final");
    }

}
