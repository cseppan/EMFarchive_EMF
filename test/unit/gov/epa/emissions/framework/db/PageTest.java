package gov.epa.emissions.framework.db;

import gov.epa.emissions.commons.Record;
import junit.framework.TestCase;

public class PageTest extends TestCase {

    public void testShouldAddRecord() {
        Page page = new Page();

        page.add(new Record());
        page.add(new Record());

        assertEquals(2, page.count());
    }

    public void testShouldGetRecords() {
        Page page = new Page();

        Record record1 = new Record();
        page.add(record1);
        Record record2 = new Record();
        page.add(record2);

        Record[] records = page.getRecords();

        assertEquals(2, records.length);
        assertEquals(record1, records[0]);
        assertEquals(record2, records[1]);
    }

    public void testShouldSetRecords() {
        Page page = new Page();

        Record record1 = new Record();
        Record record2 = new Record();
        page.setRecords(new Record[] { record1, record2 });

        Record[] records = page.getRecords();

        assertEquals(2, records.length);
        assertEquals(record1, records[0]);
        assertEquals(record2, records[1]);
    }

    public void testShouldRemoveRecord() {
        Page page = new Page();

        page.add(new Record());
        page.add(new Record());

        assertTrue("Should be able to remove record 1", page.remove(1));
        assertFalse("Should be unable to remove record 12", page.remove(7));

        assertEquals(1, page.count());
    }
}
