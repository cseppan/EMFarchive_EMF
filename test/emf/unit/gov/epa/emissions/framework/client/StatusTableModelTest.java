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

        Status[] statuses = new Status[] { status1, status2 };

        model = new StatusTableModel();
        assertEquals(0, model.getRowCount());
        assertNull("No data on creation", model.getValueAt(0, 0));

        model.refresh(statuses);
        assertEquals(2, model.getRowCount());
    }

    public void testShouldReturnRowsEqualingTheNumberOfStatusMessages() {
        assertEquals(2, model.getRowCount());
    }

    public void testShouldHaveFourColumns() {
        assertEquals(3, model.getColumnCount());
    }

    public void testShouldReturnExpectedColumnsNames() {
        assertEquals("Message Type", model.getColumnName(0));
        assertEquals("Message", model.getColumnName(1));
        assertEquals("Timestamp", model.getColumnName(2));
    }

    public void testShouldReturnUserAttributesAtSpecifiedIndex() {
        assertEquals(status1.getMessageType(), model.getValueAt(0, 0));
        assertEquals(status1.getMessage(), model.getValueAt(0, 1));
        assertEquals(status1.getTimestamp(), model.getValueAt(0, 2));

        assertEquals(status2.getMessageType(), model.getValueAt(1, 0));
        assertEquals(status2.getMessage(), model.getValueAt(1, 1));
        assertEquals(status2.getTimestamp(), model.getValueAt(1, 2));
    }

    public void testShouldAppendStatusesOnRefresh() {
        Status status = new Status("user2", "type2", "message2", new Date());
        Status[] statuses = new Status[] { status };

        model.refresh(statuses);

        assertEquals(3, model.getRowCount());
    }

    public void testShouldClearStatusesOnClear() {
        Status status = new Status("user2", "type2", "message2", new Date());
        Status[] statuses = new Status[] { status };

        model.refresh(statuses);

        model.clear();

        assertEquals(0, model.getRowCount());
    }
}
