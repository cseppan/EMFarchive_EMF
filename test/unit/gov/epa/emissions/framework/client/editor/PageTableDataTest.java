package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.framework.services.Page;
import junit.framework.TestCase;

public class PageTableDataTest extends TestCase {

    public void testShouldHaveFiveColumns() {
        InternalSource source = new InternalSource();
        source.setCols(new String[] { "col1", "col2", "col3" });

        PageTableData data = new PageTableData(source, new Page());

        String[] columns = data.columns();
        assertEquals(3, columns.length);
        assertEquals(source.getCols()[0], columns[0]);
        assertEquals(source.getCols()[1], columns[1]);
        assertEquals(source.getCols()[2], columns[2]);
    }
    
    public void testShouldMarkAllColumnsAsNotEditable() {
        InternalSource source = new InternalSource();
        source.setCols(new String[] { "col1", "col2", "col3" });
        
        PageTableData data = new PageTableData(source, new Page());
        
        assertFalse("All columns should not be editable", data.isEditable(0));
        assertFalse("All columns should not be editable", data.isEditable(1));
        assertFalse("All columns should not be editable", data.isEditable(2));
    }
}
