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
        
        record1.setA(1);
        record1.setB(1);
        record1.setCostYear(1901);
        
        record2.setA(2);
        record2.setB(2);
        record2.setCostYear(1902);
        
        record3.setA(3);
        record3.setB(3);
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
        assertEquals("Discount Rate", data.columns()[2]);
        assertEquals("Slope", data.columns()[3]);
        assertEquals("Constant", data.columns()[4]);
    }
    
    public void testShouldReturnCorrectRowSource() {
        CostRecord[] rows = data.sources();
        
        assertEquals(3, rows.length);
        assertEquals(1901, rows[0].getCostYear());
        assertEquals(1902, rows[1].getCostYear());
        assertEquals(1903, rows[2].getCostYear());
    }
}
