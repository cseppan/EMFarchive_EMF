package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.services.DataEditorService;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class VersionsPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayTableViewOnView() {
        Version version = new Version();
        String table = "table";

        Mock dataView = mock(VersionedDataView.class);
        dataView.expects(once()).method("display").with(same(version), eq(table));

        Mock service = mock(DataEditorService.class);

        VersionsPresenter presenter = new VersionsPresenter((DataEditorService)service.proxy());
        presenter.doView(version, table, (VersionedDataView) dataView.proxy());
    }

}
