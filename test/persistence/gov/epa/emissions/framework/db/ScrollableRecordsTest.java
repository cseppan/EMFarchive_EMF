package gov.epa.emissions.framework.db;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.SimpleDataset;
import gov.epa.emissions.commons.io.orl.ORLNonPointImporter;
import gov.epa.emissions.framework.PersistenceTestCase;

import java.io.File;
import java.util.Random;

public class ScrollableRecordsTest extends PersistenceTestCase {

    private ScrollableRecords results;

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
        ORLNonPointImporter importer = new ORLNonPointImporter(emissions(), dataTypes());

        importer.preCondition(new File("test/data/orl/nc"), "arinv.nonpoint.nti99_NC.txt");

        Dataset dataset = new SimpleDataset();
        dataset.setName("test");
        dataset.setDatasetid(new Random().nextLong());

        importer.run(dataset);
    }

    private void dropNonPoint() throws Exception {
        super.dropTable(emissions(), "test");
    }

    public void testRowCount() throws Exception {
        assertEquals(394, results.rowCount());
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
        Record[] records = results.range(3, 7);
        assertNotNull("Should be able to fetch a range of records", records);
        assertEquals(5, records.length);
    }

    public void testIterate() throws Exception {
        for (int i = 0; i < 394; i++) {
            assertTrue("Should have more records", results.available());
            assertNotNull("Should be able to iterate through records", results.next());
        }
    }
}
