package gov.epa.emissions.framework.db;

import gov.epa.emissions.framework.services.Page;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class PageReaderTest extends MockObjectTestCase {

    public void testShouldGetPageCount() throws Exception {
        Mock scrollableRecords = mock(ScrollableRecordsStub.class);
        scrollableRecords.stubs().method("rowCount").withNoArguments().will(returnValue(new Integer(1800)));

        PageReader reader = new PageReader(3, (ScrollableRecords) scrollableRecords.proxy());

        assertEquals(600, reader.count());
    }

    public void testShouldGetSpecifiedPage() throws Exception {
        Mock scrollableRecords = mock(ScrollableRecordsStub.class);
        scrollableRecords.stubs().method("rowCount").withNoArguments().will(returnValue(new Integer(1800)));

        PageReader reader = new PageReader(3, (ScrollableRecords) scrollableRecords.proxy());

        Page page = reader.page(5);
        assertNotNull("Should be able to fetch Page 5", page);
        
        assertNull("Page 1801 does not exist", reader.page(1801));
    }
}
