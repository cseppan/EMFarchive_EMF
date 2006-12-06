package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.client.meta.logs.LogsTableData;
import gov.epa.emissions.framework.services.basic.AccessLog;

import java.util.List;

import junit.framework.TestCase;

public class LogsTableDataTest extends TestCase {

    private LogsTableData data;

    protected void setUp() {
        AccessLog[] logs = new AccessLog[] { new AccessLog(), new AccessLog() };

        data = new LogsTableData(logs);
    }

    public void testShouldHaveFiveColumns() {
        String[] columns = data.columns();
        assertEquals(10, columns.length);
        assertEquals("User", columns[0]);
        assertEquals("Dataset Name", columns[1]);
        assertEquals("Version", columns[2]);
        assertEquals("Access Date", columns[3]);
        assertEquals("Start Date", columns[4]);
        assertEquals("End Date", columns[5]);
        assertEquals("Lines Exported", columns[6]);
        assertEquals("Time Reqrd (ms)", columns[7]);
        assertEquals("Description", columns[8]);
        assertEquals("Export Location", columns[9]);
    }

    public void testShouldReturnStringAsColumnClassForAllOtherColumns() {
        assertEquals(String.class, data.getColumnClass(0));
        assertEquals(String.class, data.getColumnClass(1));
        assertEquals(String.class, data.getColumnClass(2));
        assertEquals(String.class, data.getColumnClass(3));
        assertEquals(String.class, data.getColumnClass(4));
        assertEquals(String.class, data.getColumnClass(5));
        assertEquals(String.class, data.getColumnClass(6));
        assertEquals(String.class, data.getColumnClass(7));
        assertEquals(String.class, data.getColumnClass(8));
        assertEquals(String.class, data.getColumnClass(9));
    }

    public void testAllColumnsShouldBeUneditable() {
        assertFalse("All cells should be uneditable", data.isEditable(0));
        assertFalse("All cells should be uneditable", data.isEditable(1));
        assertFalse("All cells should be uneditable", data.isEditable(2));
        assertFalse("All cells should be uneditable", data.isEditable(3));
        assertFalse("All cells should be uneditable", data.isEditable(4));
        assertFalse("All cells should be uneditable", data.isEditable(5));
        assertFalse("All cells should be uneditable", data.isEditable(6));
        assertFalse("All cells should be uneditable", data.isEditable(7));
        assertFalse("All cells should be uneditable", data.isEditable(8));
        assertFalse("All cells should be uneditable", data.isEditable(9));
    }

    public void testShouldReturnTheRowsCorrespondingToAccessLogCountObtainedFromLoggingServices() {
        List rows = data.rows();
        assertNotNull("Should have 2 rows", rows);
        assertEquals(2, rows.size());
    }

    public void testShouldReturnARowRepresentingAnAccessLogEntry() {
        AccessLog log1 = new AccessLog();
        log1.setUsername("user1");

        AccessLog log2 = new AccessLog();
        AccessLog[] logs = new AccessLog[] { log1, log2 };

        data = new LogsTableData(logs);

        assertEquals(log1, data.element(0));
        assertEquals(log2, data.element(1));
    }
}
