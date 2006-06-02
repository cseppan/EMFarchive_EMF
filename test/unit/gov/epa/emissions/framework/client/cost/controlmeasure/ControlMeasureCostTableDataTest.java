package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.cost.data.CostRecord;
import junit.framework.TestCase;

public class ControlMeasureCostTableDataTest extends TestCase {

    private CostRecord[] records;
    
    private ControlMeasureCostTableData data;
    
    public void setUp(){
        CostRecord record1 = new CostRecord();
        CostRecord record2 = new CostRecord();
        CostRecord record3 = new CostRecord();
        
        record1.setCostPerTon(101);
        record1.setCostYear(1901);
        
        record2.setCostPerTon(102);
        record2.setCostYear(1902);
        
        record3.setCostPerTon(103);
        record3.setCostYear(1903);
        
        records = new CostRecord[3];
        records[0] = record1;
        records[1] = record2;
        records[2] = record3;
        
        data = new ControlMeasureCostTableData(records);
    }
    
    public void testShouldReturnCorrectReturnTypes() {
        assertEquals(String.class, data.getColumnClass(1));
        assertEquals(Integer.class, data.getColumnClass(2));
        assertEquals(Float.class, data.getColumnClass(3));
        assertEquals(Float.class, data.getColumnClass(4));
        assertEquals(Float.class, data.getColumnClass(5));
    }

    public void testShouldReturnCorrectColNames() {
        assertEquals("Pollutant", data.columns()[0]);
        assertEquals("Cost Year", data.columns()[1]);
        assertEquals("Cost per Ton", data.columns()[2]);
    }
    
    public void testShouldReturnCorrectRowSource() {
        CostRecord[] rows = data.sources();
        
        assertEquals(3, rows.length);
        assertEquals(1901, rows[0].getCostYear());
        assertEquals(1902, rows[1].getCostYear());
        assertEquals(1903, rows[2].getCostYear());
    }
}
