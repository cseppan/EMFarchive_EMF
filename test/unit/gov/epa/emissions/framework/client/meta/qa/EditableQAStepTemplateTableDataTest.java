package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.framework.ui.Row;

import java.util.List;

import junit.framework.TestCase;

public class EditableQAStepTemplateTableDataTest extends TestCase {

    private EditableQAStepTemplateTableData data;

    private QAStepTemplate template1;

    private QAStepTemplate template2;

    protected void setUp() {
        template1 = new QAStepTemplate();
        template1.setName("name1");
        template1.setProgram("program1");
        template1.setProgramArguments("program-args1");
        template1.setRequired(true);
        template1.setOrder("1");

        template2 = new QAStepTemplate();
        template2.setName("name2");
        template2.setProgram("program2");
        template2.setProgramArguments("program-args2");
        template2.setRequired(false);
        template2.setOrder("2");

        data = new EditableQAStepTemplateTableData(new QAStepTemplate[] { template1, template2 });
    }

    public void testShouldHaveSixColumns() {
        String[] columns = data.columns();
        
        assertEquals(6, columns.length);
        assertEquals("Select", columns[0]);
        assertEquals("Name", columns[1]);
        assertEquals("Program", columns[2]);
        assertEquals("Arguments", columns[3]);
        assertEquals("Required", columns[4]);
        assertEquals("Order", columns[5]);
    }

    public void testShouldReturnStringAsColumnClassForAllColsExceptSelectedAndRequiredCol() {
        assertEquals(Boolean.class, data.getColumnClass(0));
        assertEquals(String.class, data.getColumnClass(1));
        assertEquals(String.class, data.getColumnClass(2));
        assertEquals(String.class, data.getColumnClass(3));
        assertEquals(Boolean.class, data.getColumnClass(4));
        assertEquals(String.class, data.getColumnClass(5));
    }

    public void testAllColumnsShouldBeUneditableExceptSelect() {
        assertTrue("Select column should be editable", data.isEditable(0));
        
        for (int i = 1; i < 5; i++) {
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
        assertEquals(template1.getName(), row.getValueAt(1));
        assertEquals(template1.getProgram(), row.getValueAt(2));
        assertEquals(template1.getProgramArguments(), row.getValueAt(3));
        assertEquals(template1.isRequired(), ((Boolean) row.getValueAt(4)).booleanValue());
        assertEquals(template1.getOrder(), row.getValueAt(5));
    }

    public void testShouldReturnARowRepresentingATemplateEntry() {
        assertEquals(template1, data.element(0));
        assertEquals(template2, data.element(1));
    }
    
    public void testShouldRemoveSelectedRow() {
        data.setValueAt(Boolean.TRUE, 1, 0);
        data.removeSelected();
        
        List rows = data.rows();
        assertEquals("Rows number now is 1.", 1, rows.size());
        assertEquals(template1, data.element(0));
    }
    
    public void testShouldReturnCorrectSources() {
        QAStepTemplate[] sources = data.sources();
        
        assertEquals(template1, sources[0]);
        assertEquals(template2, sources[1]);
    }
    
    public void testShouldAddNewQAStepTemplateOnAddMethod() {
        QAStepTemplate template3 = new QAStepTemplate();
        template3.setName("name3");
        template3.setProgram("program3");
        template3.setProgramArguments("program-args3");
        template3.setRequired(false);
        template3.setOrder("3");
        
        data.add(template3);
        
        List rows = data.rows();
        Row row = (Row) rows.get(2);
        
        assertEquals("Rows number is now 3", 3, rows.size());
        assertEquals(template3.getName(), row.getValueAt(1));
    }

}
