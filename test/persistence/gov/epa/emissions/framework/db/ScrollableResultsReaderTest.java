package gov.epa.emissions.framework.db;

import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.SimpleDataset;
import gov.epa.emissions.commons.io.orl.ORLNonPointImporter;
import gov.epa.emissions.framework.PersistenceTestCase;

import java.io.File;
import java.sql.ResultSet;
import java.util.Random;

public class ScrollableResultsReaderTest extends PersistenceTestCase {

    protected void setUp() throws Exception {
        super.setUp();
        importNonPoint();
    }

    protected void tearDown() throws Exception {
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

    public void testScrollForwardAndReverse() throws Exception {
        ResultSet rs = scrollableResultSet(emissions(), "SELECT * from emissions.test");

        assertTrue(rs.next());
        assertEquals(1, rs.getRow());

        assertTrue(rs.absolute(5));
        assertEquals(5, rs.getRow());

        assertTrue(rs.relative(-2));
        assertEquals(3, rs.getRow());
    }
}
