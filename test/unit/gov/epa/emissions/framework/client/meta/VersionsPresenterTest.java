package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.editor.DataView;
import gov.epa.emissions.framework.client.editor.DataViewPresenter;
import gov.epa.emissions.framework.client.editor.EditableDataView;
import gov.epa.emissions.framework.client.editor.EditableDataViewPresenter;
import gov.epa.emissions.framework.services.DataEditorService;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.constraint.IsInstanceOf;

public class VersionsPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayTableViewOnView() {
        Version version = new Version();
        String table = "table";
        Mock service = mock(DataEditorService.class);
        DataEditorService serviceProxy = (DataEditorService) service.proxy();

        Mock dataView = mock(DataView.class);
        dataView.expects(once()).method("display").with(same(version), eq(table), same(serviceProxy));
        dataView.expects(once()).method("observe").with(new IsInstanceOf(DataViewPresenter.class));

        VersionsPresenter presenter = new VersionsPresenter(serviceProxy);
        presenter.doView(version, table, (DataView) dataView.proxy());
    }
    
    public void testShouldDisplayEditableTableViewOnEdit() {
        Version version = new Version();
        String table = "table";
        Mock service = mock(DataEditorService.class);
        DataEditorService serviceProxy = (DataEditorService) service.proxy();
        
        Mock dataView = mock(EditableDataView.class);
        dataView.expects(once()).method("display").with(same(version), eq(table), same(serviceProxy));
        dataView.expects(once()).method("observe").with(new IsInstanceOf(EditableDataViewPresenter.class));
        
        VersionsPresenter presenter = new VersionsPresenter(serviceProxy);
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

        VersionsPresenter presenter = new VersionsPresenter(serviceProxy);
        view.expects(once()).method("observe").with(same(presenter));

        presenter.observe((VersionsView) view.proxy());

        presenter.doNew(version, derivedName);
    }

    public void testShouldObserveViewOnObserve() {
        Mock view = mock(VersionsView.class);

        VersionsPresenter presenter = new VersionsPresenter(null);
        view.expects(once()).method("observe").with(same(presenter));

        presenter.observe((VersionsView) view.proxy());
    }

}
