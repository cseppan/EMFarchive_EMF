package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.editor.DataView;
import gov.epa.emissions.framework.client.editor.DataViewPresenter;
import gov.epa.emissions.framework.client.editor.EditableDataView;
import gov.epa.emissions.framework.client.editor.EditableDataViewPresenter;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.EmfDataset;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.constraint.IsInstanceOf;

public class VersionsPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayTableViewOnView() throws Exception {
        Version version = new Version();
        String table = "table";

        Mock service = mock(DataEditorService.class);
        service.expects(once()).method("openSession").withAnyArguments();
        DataEditorService serviceProxy = (DataEditorService) service.proxy();

        Mock dataView = mock(DataView.class);
        dataView.expects(once()).method("display").with(same(version), eq(table), same(serviceProxy));
        dataView.expects(once()).method("observe").with(new IsInstanceOf(DataViewPresenter.class));

        VersionsPresenter presenter = new VersionsPresenter(null, serviceProxy);
        presenter.doView(version, table, (DataView) dataView.proxy());
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

        VersionsPresenter presenter = new VersionsPresenter(null, serviceProxy);
        presenter.doEdit(version, table, (EditableDataView) dataView.proxy());
    }

    public void testShouldDeriveNewVersionOnNew() throws Exception {
        Version version = new Version();
        Version derived = new Version();
        String derivedName = "name";

        Mock service = mock(DataEditorService.class);
        service.expects(once()).method("derive").with(same(version), eq(derivedName)).will(returnValue(derived));
        DataEditorService serviceProxy = (DataEditorService) service.proxy();

        Mock view = mock(VersionsView.class);
        view.expects(once()).method("add").with(same(derived));

        VersionsPresenter presenter = new VersionsPresenter(null, serviceProxy);
        view.expects(once()).method("observe").with(same(presenter));

        presenter.observe((VersionsView) view.proxy());

        presenter.doNew(version, derivedName);
    }

    public void testShouldObserveViewOnObserve() {
        Mock view = mock(VersionsView.class);

        VersionsPresenter presenter = new VersionsPresenter(null, null);
        view.expects(once()).method("observe").with(same(presenter));

        presenter.observe((VersionsView) view.proxy());
    }

    public void testShouldMarkVersionAsFinalOnMarkFinal() throws Exception {
        Version version = new Version();
        version.setVersion(8);

        Mock service = mock(DataEditorService.class);
        service.expects(once()).method("markFinal").with(same(version)).will(returnValue(new Version()));

        Version[] versions = {};
        service.expects(once()).method("getVersions").with(eq(new Long(45))).will(returnValue(versions));

        EmfDataset dataset = new EmfDataset();
        dataset.setDatasetid(45);

        Mock view = mock(VersionsView.class);
        view.expects(once()).method("reload").with(same(versions));

        VersionsPresenter p = new VersionsPresenter(dataset, (DataEditorService) service.proxy());
        view.expects(once()).method("observe").with(same(p));
        p.observe((VersionsView) view.proxy());

        p.doMarkFinal(new Version[] { version });
    }

    public void testShouldRaiseErrorOnMarkFinalWhenVersionIsAlreadyFinal() throws Exception {
        Version version = new Version();
        version.setVersion(2);
        version.markFinal();

        VersionsPresenter p = new VersionsPresenter(null, null);

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
