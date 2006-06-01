package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.cost.ControlMeasure;
import junit.framework.TestCase;

public class ControlMeasureTableDataTest extends TestCase {

    public void testShouldAppropriateColumnClassDefinedForAllColumns() {
        ControlMeasureTableData data = new ControlMeasureTableData(new ControlMeasure[0]);

        assertEquals(String.class, data.getColumnClass(1));
        assertEquals(String.class, data.getColumnClass(2));
        assertEquals(String.class, data.getColumnClass(3));
        assertEquals(Float.class, data.getColumnClass(4));
        assertEquals(Float.class, data.getColumnClass(5));
        assertEquals(Float.class, data.getColumnClass(9));
        assertEquals(String.class, data.getColumnClass(6));
        assertEquals(String.class, data.getColumnClass(7));
        assertEquals(String.class, data.getColumnClass(8));
        assertEquals(String.class, data.getColumnClass(10));
        assertEquals(String.class, data.getColumnClass(11));
        assertEquals(Integer.class, data.getColumnClass(12));
    }
}
