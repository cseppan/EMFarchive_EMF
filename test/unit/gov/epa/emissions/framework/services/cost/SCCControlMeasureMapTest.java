package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.framework.services.cost.analysis.SCCControlMeasureMap;
import gov.epa.emissions.framework.services.cost.data.CostRecord;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import junit.framework.TestCase;

public class SCCControlMeasureMapTest extends TestCase {
    
    public void testShouldSelectNoControlsAs_Source_SCCDoesNotMatchAny_CMs(){
        String[] cmSccs = new String[]{"4343561"};
        ControlMeasure cm1 = controlMeasure("cm1", 0.0,0.0, cmSccs);
        
        String[] inventorySCCs = {"20211501","40150201"};
        SCCControlMeasureMap sccCMMap = new SCCControlMeasureMap(inventorySCCs,new ControlMeasure[]{cm1},"NOx",2000);
        assertNull(sccCMMap.getMaxRedControlMeasure(inventorySCCs[0]));
        assertNull(sccCMMap.getMaxRedControlMeasure(inventorySCCs[1]));
        
    }
    
    public void testShouldReturnNullWhenNoControlMeasureSpecified(){
        
        String[] inventorySCCs = {"20211501","40150201"};
        SCCControlMeasureMap sccCMMap = new SCCControlMeasureMap(inventorySCCs,new ControlMeasure[]{},"NOx",2000);
        assertNull(sccCMMap.getMaxRedControlMeasure(inventorySCCs[0]));
        assertNull(sccCMMap.getMaxRedControlMeasure(inventorySCCs[1]));
        
    }
    
    public void testShouldSelectTheOnlyControlMeasureAvailable(){
        String[] cmSccs = {"1020302","20211501","40150201"};
        ControlMeasure cm1 = controlMeasure("cm1", 0.0,0.0, cmSccs);
        
        String[] inventorySCCs = {"20211501","40150201"};
        SCCControlMeasureMap sccCMMap = new SCCControlMeasureMap(inventorySCCs,new ControlMeasure[]{cm1},"NOx",2000);
        assertEquals("cm1",sccCMMap.getMaxRedControlMeasure(inventorySCCs[0]).getName());
        assertEquals("cm1",sccCMMap.getMaxRedControlMeasure(inventorySCCs[1]).getName());
        
    }
    
    
    public void testShouldSelectControlMeasureWithMostEfficiency(){
        String[] cmSccs1 = {"1020302","20211501","40150201"};
        ControlMeasure cm1 = controlMeasure("cm1", 0.0,0.0, cmSccs1);
        
        String[] cmSccs2 = {"1020402","20211501","40150201"};
        ControlMeasure cm2 = controlMeasure("cm2", 10.0,0.0, cmSccs2);
        
        String[] cmSccs3 = {"1020302","20211501","40150401"};
        ControlMeasure cm3 = controlMeasure("cm3", -10.0,0.0, cmSccs3);
        
        
        String[] inventorySCCs = {"20211501","40150201"};
        ControlMeasure[] controlMeasures = new ControlMeasure[]{cm1,cm2,cm3};
        
        SCCControlMeasureMap sccCMMap = new SCCControlMeasureMap(inventorySCCs,controlMeasures,"NOx",2000);
        assertEquals("cm2",sccCMMap.getMaxRedControlMeasure(inventorySCCs[0]).getName());
        assertEquals("cm2",sccCMMap.getMaxRedControlMeasure(inventorySCCs[1]).getName());
        
    }
    
    public void testShouldSelectLeastCostControlMeasureWhenMoreThanOneControlMeasureHasMaxEfficiency(){
        String[] cmSccs1 = {"1020302","20211501","40150201"};
        ControlMeasure cm1 = controlMeasure("cm1", 10.0,100.0, cmSccs1);
        
        String[] cmSccs2 = {"1020402","20211501","40150201"};
        ControlMeasure cm2 = controlMeasure("cm2", 10.0,0.0, cmSccs2);
        
        String[] cmSccs3 = {"1020702","20211501","40150401"};
        ControlMeasure cm3 = controlMeasure("cm3", 0.0,80.0, cmSccs3);
        
        String[] cmSccs4 = {"1067302","20211501","40150401"};
        ControlMeasure cm4 = controlMeasure("cm4", 10.0,80.0, cmSccs4);
        
        
        String[] inventorySCCs = {"20211501","40150201"};
        ControlMeasure[] controlMeasures = new ControlMeasure[]{cm1,cm2,cm3,cm4};
        
        SCCControlMeasureMap sccCMMap = new SCCControlMeasureMap(inventorySCCs,controlMeasures,"NOx",2000);
        assertEquals("cm2",sccCMMap.getMaxRedControlMeasure(inventorySCCs[0]).getName());
        assertEquals("cm2",sccCMMap.getMaxRedControlMeasure(inventorySCCs[1]).getName());
        
    }
    
    
    public void testShouldSelect_One_Of_the_ControlMeasures_WithEqualEfficiencyAndCost(){
        String[] cmSccs1 = {"1020302","20211501","40150201"};
        ControlMeasure cm1 = controlMeasure("cm1", 10.0,100.0, cmSccs1);
        
        String[] cmSccs2 = {"1020402","20211501","40150201"};
        ControlMeasure cm2 = controlMeasure("cm2", 10.0,100.0, cmSccs2);
        
        String[] cmSccs3 = {"1020702","20211501","40150401"};
        ControlMeasure cm3 = controlMeasure("cm3", 0.0,80.0, cmSccs3);
        
        String[] cmSccs4 = {"1067302","20211501","40150401"};
        ControlMeasure cm4 = controlMeasure("cm4", 10.0,100.0, cmSccs4);
        
        
        String[] inventorySCCs = {"20211501","40150201"};
        ControlMeasure[] controlMeasures = new ControlMeasure[]{cm1,cm2,cm3,cm4};
        
        SCCControlMeasureMap sccCMMap = new SCCControlMeasureMap(inventorySCCs,controlMeasures,"NOx",2000);
        assertEquals("cm1",sccCMMap.getMaxRedControlMeasure(inventorySCCs[0]).getName());
        assertEquals("cm1",sccCMMap.getMaxRedControlMeasure(inventorySCCs[1]).getName());
        
    }

    private ControlMeasure controlMeasure(String name, double addEfficiency, double addCost, String[] cmSccs) {
        ControlMeasure cm = new ControlMeasure();
        cm.setName(name);
        
        EfficiencyRecord efficiencyRecord = new EfficiencyRecord();
        efficiencyRecord.setPollutant("NOx");
        efficiencyRecord.setEfficiency((float) (50+addEfficiency));
        cm.setEfficiencyRecords(new EfficiencyRecord[]{efficiencyRecord});
        
        cm.setCostRecords(costRecords(addCost));
        cm.setSccs(cmSccs);
        return cm;
    }

    private CostRecord[] costRecords(double addCost) {
        CostRecord costRecord1 = new CostRecord();
        costRecord1.setCostPerTon(800);
        costRecord1.setCostYear(1999);
        costRecord1.setPollutant("NOx");
        
        CostRecord costRecord2 = new CostRecord();
        costRecord2.setCostPerTon((float) (800+addCost));
        costRecord2.setCostYear(2000);
        costRecord2.setPollutant("NOx");
        
        return new CostRecord[]{costRecord1,costRecord2};
    }

}
