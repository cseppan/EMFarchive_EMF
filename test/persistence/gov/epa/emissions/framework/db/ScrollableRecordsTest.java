package gov.epa.emissions.framework.db;

import gov.epa.emissions.commons.io.SimpleDataset;
import gov.epa.emissions.commons.io.orl.ORLNonPointImporter;
import gov.epa.emissions.framework.PersistenceTestCase;
import gov.epa.emissions.framework.services.DbRecord;

import java.io.File;
import java.util.Random;

public class ScrollableRecordsTest extends PersistenceTestCase {

    private ScrollableRecords results;

    private SimpleDataset dataset;

    protected void setUp() throws Exception {
        super.setUp();
        importNonPoint();

        results = new ScrollableRecords(emissions(), "SELECT * from emissions.test");
        results.execute();
    }

    protected void tearDown() throws Exception {
        results.close();

        dropNonPoint();
        super.tearDown();
    }

    private void importNonPoint() throws Exception {
        dataset = new SimpleDataset();
        dataset.setName("test");
        dataset.setDatasetid(new Random().nextLong());

        ORLNonPointImporter importer = new ORLNonPointImporter(dataset, emissions(), dataTypes());

        importer.preCondition(new File("test/data/orl/nc"), "arinv.nonpoint.nti99_NC.txt");
        importer.run(dataset);
    }

    private void dropNonPoint() throws Exception {
        super.dropTable(emissions(), "test");
    }

    public void testRowCount() throws Exception {
        assertEquals(394, results.total());
    }

    public void testScrollForward() throws Exception {
        assertEquals(0, results.position());
        results.forward(10);
        assertEquals(10, results.position());
    }

    public void testScrollBackward() throws Exception {
        assertEquals(0, results.position());
        results.forward(10);
        results.backward(3);

        assertEquals(7, results.position());
    }

    public void testMoveToSpecificPosition() throws Exception {
        results.moveTo(3);
        assertEquals(3, results.position());
    }

    public void testFetchRangeOfRecords() throws Exception {
        DbRecord[] records = results.range(3, 7);
        assertNotNull("Should be able to fetch a range of records", records);
        assertEquals(5, records.length);
        for (int i = 0; i < records.length; i++)
            assertNotNull(records[i]);
    }

    public void testFetchRecordsOutOfRangeShouldReturnOnlyValidPartialRange() throws Exception {
        assertRecords(6, results.range(388, 397));
        assertRecords(93, results.range(301, 399));
        assertRecords(394, results.range(0, 399));
        assertRecords(10, results.range(0, 9));
    }

    private void assertRecords(int expected, DbRecord[] range) {
        assertEquals(expected, range.length);
        for (int i = 0; i < range.length; i++)
            assertNotNull(range[i]);
    }

    public void testIterate() throws Exception {
        for (int i = 0; i < 394; i++) {
            assertTrue("Should have more records", results.available());
            assertNotNull("Should be able to iterate through records", results.next());
        }
    }

    public void testFetchFirstRecord() throws Exception {
        DbRecord record = results.next();

        assertEquals(14, record.size());
        assertNotNull("Should be able to fetch first record", record);

        assertEquals(dataset.getDatasetid() + "", record.token(0));
        assertEquals("37001", record.token(1));
        assertEquals("10201302", record.token(2));
        assertEquals("0", record.token(3));
        assertEquals("0107", record.token(4));
        assertEquals("2", record.token(5));
        assertEquals("0", record.token(6));
        assertEquals("246", record.token(7));
        assertEquals("0.000387296", record.token(8));
        assertNull(record.token(9));
        assertNull(record.token(10));
        assertNull(record.token(11));
        assertNull(record.token(12));
        assertEquals("", record.token(13));
    }

}
