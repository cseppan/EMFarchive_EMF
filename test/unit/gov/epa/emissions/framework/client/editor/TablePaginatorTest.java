package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.VersionedRecord;
import gov.epa.emissions.framework.services.DataAccessService;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class TablePaginatorTest extends MockObjectTestCase {

    public void testShouldNotDisplayPageIfRecordRequestedExistsOnCurrentPage() throws Exception {
        Mock service = mock(DataAccessService.class);
        
        Page page = new Page();
        page.setMin(20);
        page.add(new VersionedRecord());
        service.stubs().method("getPage").will(returnValue(page));
        
        Mock view = mock(TableView.class);
        view.expects(once()).method("display");
        
        TablePaginator paginator = new TablePaginator(null, null, (TableView)view.proxy(), (DataAccessService) service.proxy());
        paginator.doDisplayFirst();
        
        paginator.doDisplayPageWithRecord(12);
    }
}
