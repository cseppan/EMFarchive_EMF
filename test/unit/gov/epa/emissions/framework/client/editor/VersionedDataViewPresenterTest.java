package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.services.DataEditorService;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class VersionedDataViewPresenterTest extends MockObjectTestCase {

    public void testShouldLoadTablesOfDatasetOnDisplay() throws Exception {
        Version version = new Version();
        String table = "table";

        Mock view = mock(VersionedDataView.class);
        view.expects(once()).method("display").with(eq(version), eq(table));

        VersionedDataViewPresenter p = new VersionedDataViewPresenter(version, table, (VersionedDataView) view.proxy(),
                null);

        p.display();
    }

    public void testShouldCloseViewAndCloseDataEditSessionOnClose() throws Exception {
        Mock view = mock(VersionedDataView.class);
        view.expects(once()).method("close").withNoArguments();

        Mock services = mock(DataEditorService.class);
        services.expects(once()).method("close").withNoArguments();

        VersionedDataViewPresenter p = new VersionedDataViewPresenter(null, null, (VersionedDataView) view.proxy(),
                (DataEditorService) services.proxy());

        p.doClose();
    }

}
