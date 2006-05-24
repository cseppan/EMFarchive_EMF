package gov.epa.emissions.framework.client.cost.controlmeasure;

import java.util.List;

import junit.framework.TestCase;

public class SCCTableDataTest extends TestCase {

    private SCCTableData data;

    private Scc[] sccs;

    protected void setUp() {
        sccs = new Scc[2];
        sccs[0] = new Scc("name1", "description1");
        sccs[1] = new Scc("name2", "description2");

        data = new SCCTableData(sccs);
    }

    public void testShouldHaveTwoColumns() {
        String[] columns = data.columns();
        assertEquals(2, columns.length);
        assertEquals("SCC", columns[0]);
        assertEquals("Description", columns[1]);
    }

    public void testColumnClasses() {
        assertEquals(String.class, data.getColumnClass(0));
        assertEquals(String.class, data.getColumnClass(1));
    }

    public void testAllColumnsShouldBeEditable() {
        assertEquals(false, data.isEditable(0));
        assertEquals(false, data.isEditable(1));
    }

    public void testShouldReturnTheRowsCorrespondingToCount() {
        List rows = data.rows();
        assertNotNull("Should have 2 rows", rows);
        assertEquals(2, rows.size());
    }

}
