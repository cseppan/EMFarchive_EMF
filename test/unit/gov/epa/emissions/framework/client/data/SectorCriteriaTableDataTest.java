package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.SectorCriteria;
import gov.epa.emissions.framework.ui.Row;

import java.util.List;

import junit.framework.TestCase;

public class SectorCriteriaTableDataTest extends TestCase {

    private SectorCriteriaTableData data;

    protected void setUp() {
        SectorCriteria element1 = new SectorCriteria();
        element1.setId(1);
        element1.setType("type1");
        element1.setCriteria("criterion1");

        SectorCriteria element2 = new SectorCriteria();
        element2.setId(2);
        element2.setType("type2");
        element2.setCriteria("criterion2");

        data = new SectorCriteriaTableData(new SectorCriteria[] { element1, element2 });
    }

    public void testShouldHaveTwoColumns() {
        String[] columns = data.columns();
        assertEquals(2, columns.length);
        assertEquals("Type", columns[0]);
        assertEquals("Criterion", columns[1]);
    }

    public void testAllColumnsShouldBeEditable() {
        assertTrue("All cells should be uneditable", data.isEditable(0));
        assertTrue("All cells should be uneditable", data.isEditable(1));
    }

    public void testShouldReturnTheRowsCorrespondingToTotalCount() {
        List rows = data.rows();
        assertNotNull("Should have 2 rows", rows);
        assertEquals(2, rows.size());
    }

    public void testShouldFillTheColumnsCorrectly() {
        List rows = data.rows();

        Row row = (Row) rows.get(0);
        assertEquals("type1", row.getValueAt(0));
        assertEquals("criterion1", row.getValueAt(1));
    }

    public void testShouldReturnARowRepresentingASectorCriteriaEntry() {
        SectorCriteria element1 = new SectorCriteria();
        element1.setType("type1");
        element1.setCriteria("criterion1");

        SectorCriteria element2 = new SectorCriteria();
        element2.setType("type2");
        element2.setCriteria("criterion2");

        SectorCriteriaTableData data = new SectorCriteriaTableData(new SectorCriteria[] { element1, element2 });

        assertEquals(element1, data.element(0));
        assertEquals(element2, data.element(1));
    }

    public void testShouldRemoveCriterionOnRemove() {
        SectorCriteria criterion = new SectorCriteria();
        criterion.setId(2);

        data.remove(criterion);
        assertEquals(1, data.rows().size());
        
        data.remove(new SectorCriteria());
        assertEquals(1, data.rows().size());
    }
    
    public void testShouldAddCriterionOnAdd() {
        SectorCriteria criterion = new SectorCriteria();
        criterion.setId(3);
        
        data.add(criterion);
        
        assertEquals(3, data.rows().size());
    }
}
