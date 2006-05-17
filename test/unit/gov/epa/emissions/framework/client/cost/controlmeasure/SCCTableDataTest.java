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

    public void testShouldHaveOneColumn() {
        assertEquals(1,data.columns().length);
    }
    
    public void testColumnClassShouldBeString(){
        assertEquals(String.class,data.getColumnClass(0));
    }
    
    public void testAllColumnsShouldBeUnEditable() {
        assertEquals(false,data.isEditable(0));
    }
    
    public void testShouldReturnTheRowsCorrespondingToCount() {
        List rows = data.rows();
        assertNotNull("Should have 2 rows", rows);
        assertEquals(2, rows.size());
    }
    
}
