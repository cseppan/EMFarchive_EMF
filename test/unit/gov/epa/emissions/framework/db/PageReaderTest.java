package gov.epa.emissions.framework.db;

import gov.epa.emissions.commons.Record;
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
        Record[] records = {};
        scrollableRecords.stubs().method("range").with(eq(new Integer(50)), eq(new Integer(59))).will(
                returnValue(records));
        scrollableRecords.expects(once()).method("execute").withNoArguments();

        PageReader reader = new PageReader(10, (ScrollableRecords) scrollableRecords.proxy());
        reader.init();

        Page page = reader.page(5);
        assertNotNull("Should be able to fetch Page 5", page);
        assertEquals(records.length, page.getRecords().length);

        assertNull("Page 1801 does not exist", reader.page(1801));
    }
}
