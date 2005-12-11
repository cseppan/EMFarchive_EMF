package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.editor.VersionedDataView;
import gov.epa.emissions.framework.client.editor.VersionedDataViewPresenter;
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

        Mock dataView = mock(VersionedDataView.class);
        dataView.expects(once()).method("display").with(same(version), eq(table), same(serviceProxy));
        dataView.expects(once()).method("observe").with(new IsInstanceOf(VersionedDataViewPresenter.class));

        VersionsPresenter presenter = new VersionsPresenter(serviceProxy);
        presenter.doView(version, table, (VersionedDataView) dataView.proxy());
    }

    public void testShouldObserveViewOnObserve() {
        Mock view = mock(VersionsView.class);

        VersionsPresenter presenter = new VersionsPresenter(null);
        view.expects(once()).method("observe").with(same(presenter));

        presenter.observe((VersionsView) view.proxy());
    }

}
