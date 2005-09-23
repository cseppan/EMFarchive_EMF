package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.AccessLog;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.LoggingServices;

import java.util.List;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class AccessLogTableDataTest extends MockObjectTestCase {

    private AccessLogTableData data;

    private EmfDataset dataset;

    protected void setUp() throws EmfException {
        dataset = new EmfDataset();
        dataset.setDatasetid(1);

        Mock services = mock(LoggingServices.class);
        AccessLog[] logs = new AccessLog[] { new AccessLog(), new AccessLog() };
        services.stubs().method("getAccessLogs").with(eq(dataset.getDatasetid())).will(returnValue(logs));

        data = new AccessLogTableData(dataset, (LoggingServices) services.proxy());
    }

    public void testShouldHaveFiveColumns() {
        String[] columns = data.columns();
        assertEquals(5, columns.length);
        assertEquals("User", columns[0]);
        assertEquals("Date", columns[1]);
        assertEquals("Version", columns[2]);
        assertEquals("Description", columns[3]);
        assertEquals("Export Location", columns[4]);
    }

    public void testAllColumnsShouldBeUneditable() {
        assertFalse("All cells should be uneditable", data.isEditable(0));
        assertFalse("All cells should be uneditable", data.isEditable(1));
        assertFalse("All cells should be uneditable", data.isEditable(2));
        assertFalse("All cells should be uneditable", data.isEditable(4));
    }

    public void testShouldReturnTheRowsCorrespondingToAccessLogCountObtainedFromLoggingServices() {
        List rows = data.rows();
        assertNotNull("Should have 2 rows", rows);
        assertEquals(2, rows.size());
    }

    public void testShouldReturnARowRepresentingAnAccessLogEntry() throws EmfException {
        AccessLog log1 = new AccessLog();
        log1.setUsername("user1");
        
        AccessLog log2 = new AccessLog();
        AccessLog[] logs = new AccessLog[] { log1, log2 };

        Mock services = mock(LoggingServices.class);
        services.stubs().method("getAccessLogs").with(eq(dataset.getDatasetid())).will(returnValue(logs));

        data = new AccessLogTableData(dataset, (LoggingServices) services.proxy());
        
        assertEquals(log1, data.element(0));
        assertEquals(log2, data.element(1));
    }
}
