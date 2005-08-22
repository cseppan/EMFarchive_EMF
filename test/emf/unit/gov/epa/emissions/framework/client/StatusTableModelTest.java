package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.services.Status;

import java.util.Date;

import org.jmock.MockObjectTestCase;

public class StatusTableModelTest extends MockObjectTestCase {

    private StatusTableModel model;

    private Status status2;

    private Status status1;

    protected void setUp() {
        Date status1Timestamp = new Date();
        status1 = new Status("user1", "type1", "message1", status1Timestamp);

        Date status2Timestamp = new Date(status1Timestamp.getTime() + 2000);
        status2 = new Status("user2", "type2", "message2", status2Timestamp);

        Status[] status = new Status[] { status1, status2 };

        model = new StatusTableModel();
        assertEquals(0, model.getRowCount());
        
        model.refresh(status);
        assertEquals(2, model.getRowCount());
    }

    public void testShouldReturnRowsEqualingTheNumberOfStatusMessages() {
        assertEquals(2, model.getRowCount());
    }

    public void testShouldHaveFourColumns() {
        assertEquals(4, model.getColumnCount());
    }

    public void testShouldReturnExpectedColumnsNames() {
        assertEquals("Username", model.getColumnName(0));
        assertEquals("Message Type", model.getColumnName(1));
        assertEquals("Message", model.getColumnName(2));
        assertEquals("Timestamp", model.getColumnName(3));
    }

    public void testShouldReturnUserAttributesAtSpecifiedIndex() {
        assertEquals(status1.getUserName(), model.getValueAt(0, 0));
        assertEquals(status1.getMessageType(), model.getValueAt(0, 1));
        assertEquals(status1.getMessage(), model.getValueAt(0, 2));
        assertEquals(status1.getTimestamp(), model.getValueAt(0, 3));

        assertEquals(status2.getUserName(), model.getValueAt(1, 0));
        assertEquals(status2.getMessageType(), model.getValueAt(1, 1));
        assertEquals(status2.getMessage(), model.getValueAt(1, 2));
        assertEquals(status2.getTimestamp(), model.getValueAt(1, 3));
    }

}
