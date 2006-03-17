package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.ui.Row;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class QAStepsTableDataTest extends EmfMockObjectTestCase {

    private QAStepsTableData data;

    private QAStep step1;

    private QAStep step2;

    protected void setUp() {
        step1 = new QAStep();
        step1.setVersion(2);
        step1.setName("step1");
        User user1 = new User();
        user1.setUsername("username1");
        step1.setWho(user1);
        step1.setWhen(new Date());
        step1.setProgram("program1");
        step1.setRequired(true);
        step1.setOrder(1);
        step1.setResult("result1");
        step1.setStatus("status1");

        step2 = new QAStep();
        step2.setVersion(2);
        step2.setName("step2");
        User user2 = new User();
        user2.setUsername("username2");
        step2.setWho(user2);
        step2.setWhen(new Date());
        step2.setProgram("program2");
        step2.setRequired(false);
        step2.setOrder(2);
        step2.setResult("result2");
        step2.setStatus("status2");

        data = new QAStepsTableData(new QAStep[] { step1, step2 });
    }

    public void testShouldHaveNineColumns() {
        String[] columns = data.columns();
        assertEquals(9, columns.length);
        assertEquals("Version", columns[0]);
        assertEquals("Name", columns[1]);
        assertEquals("User", columns[2]);
        assertEquals("Date", columns[3]);
        assertEquals("Program", columns[4]);
        assertEquals("Required?", columns[5]);
        assertEquals("Order", columns[6]);
        assertEquals("Result", columns[7]);
        assertEquals("Status", columns[8]);
    }

    public void testShouldReturnAppropriateColumnClassForEachCol() {
        assertEquals(Long.class, data.getColumnClass(0));
        assertEquals(String.class, data.getColumnClass(1));
        assertEquals(String.class, data.getColumnClass(2));
        assertEquals(Date.class, data.getColumnClass(3));
        assertEquals(String.class, data.getColumnClass(4));
        assertEquals(Boolean.class, data.getColumnClass(5));
        assertEquals(String.class, data.getColumnClass(6));
        assertEquals(String.class, data.getColumnClass(7));
        assertEquals(String.class, data.getColumnClass(8));
    }

    public void testAllColumnsShouldBeUneditable() {
        for (int i = 0; i < 8; i++)
            assertFalse("All cells should be uneditable", data.isEditable(1));
    }

    public void testShouldReturnTheRowsCorrespondingToTotalCount() {
        List rows = data.rows();
        assertNotNull("Should have 2 rows", rows);
        assertEquals(2, rows.size());
    }

    public void testShouldFillTheColumnsCorrectly() {
        List rows = data.rows();

        Row row = (Row) rows.get(0);
        assertEquals(new Long(step1.getVersion()), row.getValueAt(0));
        assertEquals(step1.getName(), row.getValueAt(1));
        assertEquals(step1.getWho(), row.getValueAt(2));

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        assertEquals(dateFormat.format(step1.getWhen()), row.getValueAt(3));

        assertEquals(step1.getProgram(), row.getValueAt(4));
        assertEquals(step1.isRequired(), ((Boolean) row.getValueAt(5)).booleanValue());
        assertEquals(step1.getOrder(), 0, (((Float) row.getValueAt(6)).floatValue()));
        assertEquals(step1.getResult(), row.getValueAt(7));
        assertEquals(step1.getStatus(), row.getValueAt(8));
    }

    public void testShouldReturnARowRepresentingANoteEntry() {
        assertEquals(step1, data.element(0));
        assertEquals(step2, data.element(1));
    }

}
