package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.services.DataEditorService;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.constraint.IsInstanceOf;

public class VersionsPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayTableViewOnView() {
        Version version = new Version();
        String table = "table";

        Mock dataView = mock(VersionedDataView.class);
        dataView.expects(once()).method("display").with(same(version), eq(table));
        dataView.expects(once()).method("observe").with(new IsInstanceOf(VersionedDataViewPresenter.class));

        Mock service = mock(DataEditorService.class);

        VersionsPresenter presenter = new VersionsPresenter((DataEditorService) service.proxy());
        presenter.doView(version, table, (VersionedDataView) dataView.proxy());
    }

}
