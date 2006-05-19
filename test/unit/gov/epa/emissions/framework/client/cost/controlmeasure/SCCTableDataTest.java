package gov.epa.emissions.framework.client.cost.controlmeasure;

import java.util.List;

import junit.framework.TestCase;

public class SCCTableDataTest extends TestCase {

    private SCCTableData data;

    private String[] sccs;

    protected void setUp() {
        sccs = new String[2];
        sccs[0] = "name1";
        sccs[1] = "name2";

        data = new SCCTableData(sccs);
    }

    public void testShouldHaveTwoColumns() {
        assertEquals(2,data.columns().length);
    }
    
    public void testColumnClasses(){
        assertEquals(Boolean.class,data.getColumnClass(0));
        assertEquals(String.class,data.getColumnClass(1));
    }
    
    public void testAllColumnsShouldBeEditable() {
        assertEquals(true,data.isEditable(0));
        assertEquals(true,data.isEditable(1));
    }
    
    public void testShouldReturnTheRowsCorrespondingToCount() {
        List rows = data.rows();
        assertNotNull("Should have 2 rows", rows);
        assertEquals(2, rows.size());
    }
    
}
