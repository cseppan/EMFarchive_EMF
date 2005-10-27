package gov.epa.emissions.framework.ui;

import junit.framework.TestCase;

public class EditableRowTest extends TestCase {

    public void testShouldReturnSourceObjectAsRecord() {
        Object source = new Object();
        EditableRow row = new EditableRow(source, new Object[0]);

        assertEquals(source, row.record());
    }
}
