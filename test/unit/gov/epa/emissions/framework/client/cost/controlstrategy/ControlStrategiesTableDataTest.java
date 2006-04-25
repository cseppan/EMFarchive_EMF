package gov.epa.emissions.framework.client.cost.controlstrategy;

import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.ui.Row;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

public class ControlStrategiesTableDataTest extends TestCase {

    private ControlStrategiesTableData data;

    private ControlStrategy controlStrategy1;

    private ControlStrategy controlStrategy2;

    protected void setUp() {
        controlStrategy1 = new ControlStrategy();
        controlStrategy1.setName("name1");
        controlStrategy1.setRegion(new Region("region1"));
        controlStrategy1.setLastModifiedDate(new Date());

        controlStrategy2 = new ControlStrategy();
        controlStrategy2.setName("name2");
        controlStrategy2.setRegion(new Region("region2"));
        controlStrategy2.setLastModifiedDate(new Date());

        data = new ControlStrategiesTableData(new ControlStrategy[] { controlStrategy1, controlStrategy2 });
    }

    public void testShouldHaveTwoColumns() {
        String[] columns = data.columns();
        assertEquals(3, columns.length);
        assertEquals("Name", columns[0]);
        assertEquals("Region", columns[1]);
        assertEquals("Last Modified", columns[2]);
    }

    public void testShouldHaveAppropriateColumnClassDefinedForAllColumns() {
        assertEquals(String.class, data.getColumnClass(0));
        assertEquals(String.class, data.getColumnClass(1));
        assertEquals(Date.class, data.getColumnClass(2));
    }

    public void testAllColumnsShouldBeUnEditable() {
        assertFalse("All cells should be uneditable", data.isEditable(0));
        assertFalse("All cells should be uneditable", data.isEditable(1));
        assertFalse("All cells should be uneditable", data.isEditable(2));
    }

    public void testShouldReturnTheRowsCorrespondingToCount() {
        List rows = data.rows();
        assertNotNull("Should have 2 rows", rows);
        assertEquals(2, rows.size());
    }

    public void testShouldFillTheColumnsCorrectly() {
        List rows = data.rows();

        Row row = (Row) rows.get(0);
        assertEquals("name1", row.getValueAt(0));
        assertEquals("region1", row.getValueAt(1));
        assertEquals(format(controlStrategy1.getLastModifiedDate()), row.getValueAt(2));
    }

    private String format(Date date) {
        return new SimpleDateFormat("MM/dd/yyyy HH:mm").format(date);
    }

    public void testShouldReturnARowRepresentingAControlStrategyEntry() {
        assertEquals(controlStrategy1, data.element(0));
        assertEquals(controlStrategy2, data.element(1));
    }
}
