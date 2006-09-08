package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.gui.Changeables;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.services.data.EmfDateFormat;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.ui.Row;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.jmock.Mock;

public class EditableQAStepsTableDataTest extends EmfMockObjectTestCase {

    private EditableQAStepsTableData data;

    private QAStep step1;

    private QAStep step2;

    protected void setUp() {
        step1 = new QAStep();
        step1.setName("name1");
        step1.setProgram(new QAProgram("program1"));
        step1.setProgramArguments("program-args1");
        step1.setRequired(true);
        step1.setDate(new Date());
        step1.setOrder(1);
        step1.setConfiguration("dataset one");

        step2 = new QAStep();
        step2.setName("name2");
        step2.setProgram(new QAProgram("program2"));
        step2.setProgramArguments("program-args2");
        step2.setDate(new Date());
        step2.setRequired(false);
        step2.setOrder(2);
        step2.setConfiguration("dataset two");

        data = new EditableQAStepsTableData(new QAStep[] { step1, step2 });
    }

    public void testShouldHaveTenColumns() {
        String[] columns = data.columns();

        assertEquals(11, columns.length);
        assertEquals("Version", columns[0]);
        assertEquals("Name", columns[1]);
        assertEquals("Required", columns[2]);
        assertEquals("Order", columns[3]);
        assertEquals("Status", columns[4]);
        assertEquals("When", columns[5]);
        assertEquals("Who", columns[6]);
        assertEquals("Comment", columns[7]);
        assertEquals("Program", columns[8]);
        assertEquals("Arguments", columns[9]);
        assertEquals("Configuration", columns[10]);
    }

    public void testShouldReturnCorrectTypesAsColumnClassForAllCols() {
        assertEquals(String.class, data.getColumnClass(0));
        assertEquals(String.class, data.getColumnClass(1));
        assertEquals(Boolean.class, data.getColumnClass(2));
        assertEquals(String.class, data.getColumnClass(3));
        assertEquals(String.class, data.getColumnClass(4));
        assertEquals(Date.class, data.getColumnClass(5));
        assertEquals(User.class, data.getColumnClass(6));
        assertEquals(String.class, data.getColumnClass(7));
        assertEquals(String.class, data.getColumnClass(8));
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
        assertEquals(step1.getVersion(), ((Integer) row.getValueAt(0)).intValue());
        assertEquals(step1.getName(), row.getValueAt(1));
        assertEquals(step1.isRequired(), ((Boolean) row.getValueAt(2)).booleanValue());
        assertEquals(step1.getOrder() + "", row.getValueAt(3) + "");
        assertEquals(step1.getStatus(), row.getValueAt(4));

        SimpleDateFormat dateFormat = new SimpleDateFormat(EmfDateFormat.format());
        assertEquals(dateFormat.format(step1.getDate()), row.getValueAt(5));

        assertEquals(step1.getWho(), row.getValueAt(6));
        assertEquals(step1.getComments(), row.getValueAt(7));
        assertEquals(step1.getProgram().getName(), row.getValueAt(8));
        assertEquals(step1.getProgramArguments(), row.getValueAt(9));
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
        step3.setProgram(new QAProgram("program3"));
        step3.setProgramArguments("program-args3");
        step3.setRequired(false);
        step3.setOrder(3);

        Mock changeables = mock(Changeables.class);
        expects(changeables, 1, "onChanges");
        data.observe((Changeables) changeables.proxy());

        data.add(step3);
        assertEquals(3, data.rows().size());
        assertEquals(3, data.sources().length);
        
        List rows = data.rows();
        Row row = (Row) rows.get(2);

        assertEquals("Rows number is now 3", 3, rows.size());
        assertEquals(step3.getName(), row.getValueAt(1));
    }
    
    public void testShouldRecreateRowsOnRefresh() {
        QAStep step3 = new QAStep();
        step3.setName("name3");
        step3.setProgram(new QAProgram("program3"));
        step3.setProgramArguments("program-args3");
        step3.setRequired(false);
        step3.setOrder(3);
        
        Mock changeables = mock(Changeables.class);
        expects(changeables, 2, "onChanges");
        data.observe((Changeables) changeables.proxy());
        
        data.add(step3);
        data.refresh();
        assertEquals(3, data.rows().size());
        assertEquals(3, data.sources().length);
        
        List rows = data.rows();
        Row row = (Row) rows.get(2);
        assertEquals(step3.getName(), row.getValueAt(1));
    }

}
