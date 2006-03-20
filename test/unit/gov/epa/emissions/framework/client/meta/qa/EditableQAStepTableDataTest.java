package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.ui.Row;

import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

public class EditableQAStepTableDataTest extends TestCase {

    private EditableQAStepTableData data;

    private QAStep step1;

    private QAStep step2;

    protected void setUp() {
        step1 = new QAStep();
        step1.setName("name1");
        step1.setProgram("program1");
        step1.setProgramArguments("program-args1");
        step1.setRequired(true);
        step1.setOrder(1);

        step2 = new QAStep();
        step2.setName("name2");
        step2.setProgram("program2");
        step2.setProgramArguments("program-args2");
        step2.setRequired(false);
        step2.setOrder(2);

        data = new EditableQAStepTableData(new QAStep[] { step1, step2 });
    }

    public void testShouldHaveSixColumns() {
        String[] columns = data.columns();

        assertEquals(11, columns.length);
        assertEquals("Select", columns[0]);
        assertEquals("Version", columns[1]);
        assertEquals("Name", columns[2]);
        assertEquals("Program", columns[3]);
        assertEquals("Arguments", columns[4]);
        assertEquals("Required", columns[5]);
        assertEquals("Order", columns[6]);
        assertEquals("When", columns[7]);
        assertEquals("Who", columns[8]);
        assertEquals("Result", columns[9]);
        assertEquals("Status", columns[10]);
    }

    public void testShouldReturnCorrectTypesAsColumnClassForAllCols() {
        assertEquals(Boolean.class, data.getColumnClass(0));
        assertEquals(String.class, data.getColumnClass(1));
        assertEquals(String.class, data.getColumnClass(2));
        assertEquals(String.class, data.getColumnClass(3));
        assertEquals(String.class, data.getColumnClass(4));
        assertEquals(Boolean.class, data.getColumnClass(5));
        assertEquals(Float.class, data.getColumnClass(6));
        assertEquals(Date.class, data.getColumnClass(7));
        assertEquals(User.class, data.getColumnClass(8));
        assertEquals(String.class, data.getColumnClass(9));
        assertEquals(String.class, data.getColumnClass(10));
    }

    public void testAllColumnsShouldBeUneditableExceptSelect() {
        assertTrue("Select column should be editable", data.isEditable(0));

        for (int i = 1; i < 13; i++) {
            assertFalse("Cell index " + i + " should be uneditable", data.isEditable(i));
        }
    }

    public void testShouldReturnTheRowsCorrespondingToTotalCount() {
        List rows = data.rows();
        assertNotNull("Should have 2 rows", rows);
        assertEquals(2, rows.size());
    }

    public void testShouldFillTheColumnsCorrectly() {
        List rows = data.rows();

        Row row = (Row) rows.get(0);
        assertEquals(Boolean.FALSE, row.getValueAt(0));
        assertEquals(step1.getVersion(), Integer.parseInt((String) row.getValueAt(1)));
        assertEquals(step1.getName(), row.getValueAt(2));
        assertEquals(step1.getProgram(), row.getValueAt(3));
        assertEquals(step1.getProgramArguments(), row.getValueAt(4));
        assertEquals(step1.isRequired(), ((Boolean) row.getValueAt(5)).booleanValue());
        assertEquals(step1.getOrder(), 0.0, ((Float) row.getValueAt(6)).floatValue());
        assertEquals(step1.getWhen(), row.getValueAt(7));
        assertEquals(step1.getWho(), row.getValueAt(8));
        assertEquals(step1.getResult(), row.getValueAt(9));
        assertEquals(step1.getStatus(), row.getValueAt(10));
    }

    public void testShouldReturnARowRepresentingAQAStep() {
        assertEquals(step1, data.element(0));
        assertEquals(step2, data.element(1));
    }

    public void testShouldReturnCorrectSources() {
        QAStep[] sources = data.sources();

        assertEquals(step1, sources[0]);
        assertEquals(step2, sources[1]);
    }

    public void testShouldAddNewQAStepTemplateOnAddMethod() {
        QAStep step3 = new QAStep();
        step3.setName("name3");
        step3.setProgram("program3");
        step3.setProgramArguments("program-args3");
        step3.setRequired(false);
        step3.setOrder(3);

        data.add(step3);

        List rows = data.rows();
        Row row = (Row) rows.get(2);

        assertEquals("Rows number is now 3", 3, rows.size());
        assertEquals(step3.getName(), row.getValueAt(2));
    }

    public void testShouldReturnCorrectSelectedRows() {
        QAStep step3 = new QAStep();
        step3.setName("name3");
        step3.setProgram("program3");
        step3.setProgramArguments("program-args3");
        step3.setRequired(false);
        step3.setOrder(3);

        data.add(step3);
        data.setValueAt(Boolean.TRUE, 0, 0);
        data.setValueAt(Boolean.TRUE, 2, 0);

        QAStep[] rows = data.getSelected();

        assertEquals("Only two rows selected", 2, rows.length);
        assertEquals(step1.getName(), rows[0].getName());
        assertEquals(step3.getName(), rows[1].getName());
    }

}
