package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.cost.ControlMeasure;
import junit.framework.TestCase;

public class ControlMeasureTableDataTest extends TestCase {

    public void testShouldAppropriateColumnClassDefinedForAllColumns() {
        ControlMeasureTableData data = new ControlMeasureTableData(new ControlMeasure[0], "major", "1999");
        for (int i = 0; i < data.columns().length; i++)
            assertEquals(String.class, data.getColumnClass(i));
    }
}
