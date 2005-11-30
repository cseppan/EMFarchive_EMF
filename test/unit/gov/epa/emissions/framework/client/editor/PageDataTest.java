package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.DbRecord;
import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.framework.services.Page;
import gov.epa.emissions.framework.ui.Row;

import java.util.List;

import junit.framework.TestCase;

public class PageDataTest extends TestCase {

    public void testShouldReturnStringAsColumnClassForAllColumns() {
        PageData data = new PageData(null, new Page());

        assertEquals(String.class, data.getColumnClass(0));
        assertEquals(String.class, data.getColumnClass(1));
    }
    
    public void testShouldHaveTwoColumnsIgnoringFirstColumnForDisplayPurposes() {
        InternalSource source = new InternalSource();
        source.setCols(new String[] { "col1", "col2", "col3" });

        PageData data = new PageData(source, new Page());

        String[] columns = data.columns();
        assertEquals(2, columns.length);
        assertEquals(source.getCols()[1], columns[0]);
        assertEquals(source.getCols()[2], columns[1]);
    }

    public void testShouldMarkAllColumnsAsNotEditable() {
        InternalSource source = new InternalSource();
        source.setCols(new String[] { "col1", "col2", "col3" });

        PageData data = new PageData(source, new Page());

        assertFalse("All columns should not be editable", data.isEditable(0));
        assertFalse("All columns should not be editable", data.isEditable(1));
        assertFalse("All columns should not be editable", data.isEditable(2));
    }

    public void testShouldReturnTheRowsCorrespondingToRecordsInSpecifiedPage() {
        InternalSource source = new InternalSource();
        source.setCols(new String[] { "col1", "col2", "col3" });

        Page page = new Page();
        DbRecord record1 = new DbRecord();
        record1.setTokens(new String[] { "1", "2", "3" });
        page.add(record1);
        DbRecord record2 = new DbRecord();
        record2.setTokens(new String[] { "11", "12", "13" });
        page.add(record2);

        PageData data = new PageData(source, page);

        List rows = data.rows();
        assertNotNull("Should have 2 rows", rows);
        assertEquals(2, rows.size());
    }
    
    public void testRowsShouldContainDataValuesOfRecords() {
        InternalSource source = new InternalSource();
        source.setCols(new String[] { "col1", "col2", "col3" });
        
        Page page = new Page();
        DbRecord record1 = new DbRecord();
        record1.setTokens(new String[] { "1", "2", "3" });
        page.add(record1);
        DbRecord record2 = new DbRecord();
        record2.setTokens(new String[] { "11", "12", "13" });
        page.add(record2);
        
        PageData data = new PageData(source, page);
        
        List rows = data.rows();
        
        Row row1 = (Row) rows.get(0);
        assertEquals(record1.token(1), row1.getValueAt(0));
        assertEquals(record1.token(2), row1.getValueAt(1));
        
        Row row2 = (Row) rows.get(1);
        assertEquals(record2.token(1), row2.getValueAt(0));
        assertEquals(record2.token(2), row2.getValueAt(1));
    }
}
