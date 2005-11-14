package gov.epa.emissions.framework.db;

import gov.epa.emissions.framework.services.DbRecord;
import gov.epa.emissions.framework.services.Page;
import junit.framework.TestCase;

public class PageTest extends TestCase {

    public void testShouldAddRecord() {
        Page page = new Page();

        page.add(new DbRecord());
        page.add(new DbRecord());

        assertEquals(2, page.count());
    }
    
    public void testShouldReturnRangeRepresentingMinAndMaxRecordIds() {
        Page page = new Page();
        
        assertEquals(-1, page.min());
        assertEquals(-1, page.max());
        
        page.add(new DbRecord(1));
        page.add(new DbRecord(2));
        page.add(new DbRecord(3));
        
        assertEquals(1, page.min());
        assertEquals(3, page.max());
    }

    public void testShouldGetRecords() {
        Page page = new Page();

        DbRecord record1 = new DbRecord();
        page.add(record1);
        DbRecord record2 = new DbRecord();
        page.add(record2);

        DbRecord[] records = page.getRecords();

        assertEquals(2, records.length);
        assertEquals(record1, records[0]);
        assertEquals(record2, records[1]);
    }

    public void testShouldSetRecords() {
        Page page = new Page();

        DbRecord record1 = new DbRecord();
        DbRecord record2 = new DbRecord();
        page.setRecords(new DbRecord[] { record1, record2 });

        DbRecord[] records = page.getRecords();

        assertEquals(2, records.length);
        assertEquals(record1, records[0]);
        assertEquals(record2, records[1]);
    }

    public void testShouldRemoveRecord() {
        Page page = new Page();

        page.add(new DbRecord());
        page.add(new DbRecord());

        assertTrue("Should be able to remove record 1", page.remove(1));
        assertFalse("Should be unable to remove record 12", page.remove(7));

        assertEquals(1, page.count());
    }
}
