package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.services.Status;

import org.jmock.MockObjectTestCase;

public class StatusTableModelTest extends MockObjectTestCase {

    public void testShouldReturnRowsEqualingTheNumberOfStatusMessages() {
        Status status1 = new Status();
        Status status2 = new Status();
        Status[] status = new Status[] { status1, status2 };

        StatusTableModel model = new StatusTableModel();
        model.refresh(status);

        assertEquals(2, model.getRowCount());
    }

    public void testShouldHaveFourColumns() {
        StatusTableModel model = new StatusTableModel();
        
        assertEquals(4, model.getColumnCount());
    }

    public void testShouldReturnExpectedColumnsNames() {
        StatusTableModel model = new StatusTableModel();

        assertEquals("Username", model.getColumnName(0));
        assertEquals("Message Type", model.getColumnName(1));
        assertEquals("Message", model.getColumnName(2));
        assertEquals("Timestamp", model.getColumnName(3));
    }
}
