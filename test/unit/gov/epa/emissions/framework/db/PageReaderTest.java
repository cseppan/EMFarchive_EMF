package gov.epa.emissions.framework.db;

import gov.epa.emissions.framework.services.DbRecord;
import gov.epa.emissions.framework.services.Page;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class PageReaderTest extends MockObjectTestCase {

    public void testPageCountShouldBeTotalRecordsByPageSize() throws Exception {
        Mock scrollableRecords = mock(ScrollableRecordsStub.class);
        scrollableRecords.stubs().method("total").withNoArguments().will(returnValue(new Integer(1800)));
        scrollableRecords.stubs().method("execute").withNoArguments();

        PageReader reader = new PageReader(3, (ScrollableRecords) scrollableRecords.proxy());

        assertEquals(600, reader.totalPages());
    }

    public void testShouldCloseScrollableRecordsOnClose() throws Exception {
        Mock scrollableRecords = mock(ScrollableRecordsStub.class);
        scrollableRecords.expects(once()).method("close").withNoArguments();
        scrollableRecords.stubs().method("execute").withNoArguments();

        PageReader reader = new PageReader(3, (ScrollableRecords) scrollableRecords.proxy());

        reader.close();
    }

    public void testTotalRecordsShouldBeEqualToTotalFromScrollableRecords() throws Exception {
        Mock scrollableRecords = mock(ScrollableRecordsStub.class);
        scrollableRecords.stubs().method("total").withNoArguments().will(returnValue(new Integer(1800)));
        scrollableRecords.stubs().method("execute").withNoArguments();

        PageReader reader = new PageReader(3, (ScrollableRecords) scrollableRecords.proxy());

        assertEquals(1800, reader.totalRecords());
    }

    public void testPageCountShouldIncludeTheLastPageWhichCouldBeSparse() throws Exception {
        Mock scrollableRecords = mock(ScrollableRecordsStub.class);
        scrollableRecords.stubs().method("total").withNoArguments().will(returnValue(new Integer(394)));
        scrollableRecords.stubs().method("execute").withNoArguments();

        PageReader reader = new PageReader(10, (ScrollableRecords) scrollableRecords.proxy());

        assertEquals(40, reader.totalPages());
    }

    public void testShouldGetSpecifiedPage() throws Exception {
        Mock scrollableRecords = mock(ScrollableRecordsStub.class);
        scrollableRecords.stubs().method("total").withNoArguments().will(returnValue(new Integer(1800)));
        DbRecord[] records = {};
        scrollableRecords.stubs().method("range").with(eq(new Integer(40)), eq(new Integer(49))).will(
                returnValue(records));
        scrollableRecords.expects(once()).method("execute").withNoArguments();

        PageReader reader = new PageReader(10, (ScrollableRecords) scrollableRecords.proxy());

        Page page = reader.page(5);
        assertNotNull("Should be able to fetch Page 5", page);
        assertEquals(records.length, page.getRecords().length);

        assertNull("Page 1801 does not exist", reader.page(1801));
    }

    public void testShouldGetPageBasedOnRecordNumber() throws Exception {
        Mock scrollableRecords = mock(ScrollableRecordsStub.class);
        scrollableRecords.stubs().method("total").withNoArguments().will(returnValue(new Integer(1800)));
        scrollableRecords.expects(once()).method("execute").withNoArguments();

        PageReader reader = new PageReader(10, (ScrollableRecords) scrollableRecords.proxy());

        scrollableRecords.stubs().method("range").with(eq(new Integer(40)), eq(new Integer(49))).will(
                returnValue(new DbRecord[0]));
        assertNotNull("Should be able to fetch Page containing records 40-49", reader.pageByRecord(42));

        scrollableRecords.stubs().method("range").with(eq(new Integer(0)), eq(new Integer(9))).will(
                returnValue(new DbRecord[0]));
        assertNotNull("Should be able to fetch Page containing records 0-9", reader.pageByRecord(7));

        scrollableRecords.stubs().method("range").with(eq(new Integer(10)), eq(new Integer(19))).will(
                returnValue(new DbRecord[0]));
        assertNotNull("Should be able to fetch Page containing records 10-19", reader.pageByRecord(20));

        scrollableRecords.stubs().method("range").with(eq(new Integer(10)), eq(new Integer(19))).will(
                returnValue(new DbRecord[0]));
        assertNotNull("Should be able to fetch Page containing records 10-19", reader.pageByRecord(20));

    }
}
