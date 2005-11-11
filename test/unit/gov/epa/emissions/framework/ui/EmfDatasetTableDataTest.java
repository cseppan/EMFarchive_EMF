package gov.epa.emissions.framework.ui;

import gov.epa.emissions.framework.services.EmfDataset;

import java.util.Date;

import junit.framework.TestCase;

public class EmfDatasetTableDataTest extends TestCase {


    public void testShouldAppropriateColumnClassDefinedForAllColumns() {
        EmfDatasetTableData data = new EmfDatasetTableData(new EmfDataset[0]);
        
        assertEquals(String.class, data.getColumnClass(0));
        assertEquals(String.class, data.getColumnClass(1));
        assertEquals(String.class, data.getColumnClass(2));
        assertEquals(String.class, data.getColumnClass(3));
        assertEquals(String.class, data.getColumnClass(4));
        assertEquals(Date.class, data.getColumnClass(5));
        assertEquals(Date.class, data.getColumnClass(6));
    }
}
