package gov.epa.emissions.framework.services.cost;


import junit.framework.TestCase;
import gov.epa.emissions.framework.services.cost.analysis.common.BestMeasureEffRecord;
import gov.epa.emissions.framework.services.cost.analysis.common.Type3CostEquation;
import gov.epa.emissions.framework.services.cost.analysis.common.Type4CostEquation;
import gov.epa.emissions.framework.services.cost.analysis.common.Type5CostEquation;
import gov.epa.emissions.framework.services.cost.analysis.common.Type6CostEquation;

public class EquationTypeTest extends TestCase {

    private double tolerance = 1e-2;
    
    private double discountRate = 7.0;
    
    private double reducedEmission = 1000;
    
    private double minStackFlowRate = 5;
    
    private BestMeasureEffRecord bestMeasureEffRecord= null;
    
    public void testEquationType6() throws Exception {
        Type6CostEquation type6 = new Type6CostEquation(discountRate);
        
        type6.setUpTest(reducedEmission, 15, 0.2, bestMeasureEffRecord, minStackFlowRate);
        
        try {
            System.out.println("begin-----");
            double capRecFactor1=type6.getCapRecFactor(15, 0.2);
            double capRecFactor2=type6.getCapRecFactor(0, 0.2);
            double expCapRecFactor1=0.1098;
            double expCapRecFactor2=0.2;
            
            double operatingCostResult = type6.getOperationMaintenanceCost();
            double expectedOperatingCost = 797961.2;

            double annualCost = type6.getAnnualCost();
            double expectdAnnualCost=1176805.61;
            
            double capitalCost = type6.getCapitalCost();
            double expectedCapitalCost=3450482.3;
            
            double annualizedCapitalCost=type6.getAnnualizedCapitalCost();
            double expectedAnnualizedCCost=378844.41;

            double computedCPT=type6.getComputedCPT();
            double expectedComputedCPT=1176.81;
             
             System.out.println("begin type 6 test --------------------");
             assertTrue("Check Type 6 operating and maintenance cost", (operatingCostResult - expectedOperatingCost) < tolerance);
             assertTrue("Check Type 6 annual cost", (annualCost - expectdAnnualCost) < tolerance);
             assertTrue("Check Type 6 capital cost", (capitalCost - expectedCapitalCost) < tolerance);
             assertTrue("Check Type 6 annualized cost", (annualizedCapitalCost - expectedAnnualizedCCost) < tolerance);
             assertTrue("Check Type 6 computed CPT", (computedCPT - expectedComputedCPT) < tolerance);
             
             
             assertTrue("Check Type 6 capital recovery factor with equipment life=15 ", (capRecFactor1 - expCapRecFactor1) < tolerance);
             assertTrue("Check Type 6 capital recovery factor with equipment life=0 ", (capRecFactor2 - expCapRecFactor2) < tolerance);
             System.out.println("end type 6 test --------------------");
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }

    public void testEquationType5() throws Exception {
        Type5CostEquation type5 = new Type5CostEquation(discountRate);
        
        type5.setUpTest(reducedEmission, 15, 0.2, bestMeasureEffRecord, minStackFlowRate);
        
        try {
             double operatingCostResult = type5.getOperationMaintenanceCost();
             double expectedOperatingCost = 749912;
             
             double annualCost = type5.getAnnualCost();
             double expectdAnnualCost=1066533.75;
             
             double capitalCost = type5.getCapitalCost();
             double expectedCapitalCost=2883763.7;
             
             double annualizedCapitalCost=type5.getAnnualizedCapitalCost();
             double expectedAnnualizedCCost=316621.75;
             
             double computedCPT=type5.getComputedCPT();
             double expectedComputedCPT=1066.53;
             System.out.println("begin type 5 test --------------------");
             assertTrue("Check Type 5 operating and maintenance cost", (operatingCostResult - expectedOperatingCost) < tolerance);
             assertTrue("Check Type 5 annual cost", (annualCost - expectdAnnualCost) < tolerance);
             assertTrue("Check Type 5 capital cost", (capitalCost - expectedCapitalCost) < tolerance);
             assertTrue("Check Type 5 annualized cost", (annualizedCapitalCost - expectedAnnualizedCCost) < tolerance);
             assertTrue("Check Type 5 computed CPT", (computedCPT - expectedComputedCPT) < tolerance);
             
             double capRecFactor1=type5.getCapRecFactor(15, 0.2);
             double capRecFactor2=type5.getCapRecFactor(0, 0.2);
             double expCapRecFactor1=0.1098;
             double expCapRecFactor2=0.2;
             
             assertTrue("Check Type 5 capital recovery factor with equipment life=15 ", (capRecFactor1 - expCapRecFactor1) < tolerance);
             assertTrue("Check Type 5 capital recovery factor with equipment life=0 ", (capRecFactor2 - expCapRecFactor2) < tolerance);
             System.out.println("end type 5 test --------------------");
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
    
    public void testEquationType4() throws Exception {
        Type4CostEquation type4 = new Type4CostEquation(discountRate);
        
        type4.setUpTest(reducedEmission, 15, 0.2, bestMeasureEffRecord, minStackFlowRate);
        
        try {
            System.out.println("begin type 4 test --------------------");
             double operatingCostResult = type4.getOperationMaintenanceCost();
             double expectedOperatingCost = 75864.1;
             
             double annualCost = type4.getAnnualCost();
             double expectdAnnualCost=184566.18;
             
             double capitalCost = type4.getCapitalCost();
             double expectedCapitalCost=990049.18;
             
             double annualizedCapitalCost=type4.getAnnualizedCapitalCost();
             double expectedAnnualizedCCost=108702.08;
             
             double computedCPT=type4.getComputedCPT();
             double expectedComputedCPT=184.57;
            
             assertTrue("Check Type 4 operating and maintenance cost", (operatingCostResult - expectedOperatingCost) < tolerance);
             assertTrue("Check Type 4 annual cost", (annualCost - expectdAnnualCost) < tolerance);
             assertTrue("Check Type 4 capital cost", (capitalCost - expectedCapitalCost) < tolerance);
             assertTrue("Check Type 4 annualized cost", (annualizedCapitalCost - expectedAnnualizedCCost) < tolerance);
             assertTrue("Check Type 4 computed CPT", (computedCPT - expectedComputedCPT) < tolerance);
             
             double capRecFactor1=type4.getCapRecFactor(15, 0.2);
             double capRecFactor2=type4.getCapRecFactor(0, 0.2);
             double expCapRecFactor1=0.1098;
             double expCapRecFactor2=0.2;
             
             assertTrue("Check Type 4 capital recovery factor with equipment life=15 ", (capRecFactor1 - expCapRecFactor1) < tolerance);
             assertTrue("Check Type 4 capital recovery factor with equipment life=0 ", (capRecFactor2 - expCapRecFactor2) < tolerance);
             System.out.println("end type 4 test --------------------");
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
    
    public void testEquationType3() throws Exception {
        Type3CostEquation type3 = new Type3CostEquation(discountRate);
        
        type3.setUpTest(reducedEmission, 15, 0.2, bestMeasureEffRecord, minStackFlowRate);
        
        try {
             double operatingCostResult = type3.getOperationMaintenanceCost();
             double expectedOperatingCost = 314.5;
             
             double annualCost = type3.getAnnualCost();
             double expectdAnnualCost=87149.56;
             
             double capitalCost = type3.getCapitalCost();
             double expectedCapitalCost=790886.3;
             
             double annualizedCapitalCost=type3.getAnnualizedCapitalCost();
             double expectedAnnualizedCCost=86835.06;
             
             double computedCPT=type3.getComputedCPT();
             double expectedComputedCPT=87.15;
            
             assertTrue("Check Type 3 operating and maintenance cost", Math.abs(operatingCostResult - expectedOperatingCost) < tolerance);
             assertTrue("Check Type 3 annual cost", Math.abs(annualCost - expectdAnnualCost) < tolerance);
             assertTrue("Check Type 3 capital cost", Math.abs(capitalCost - expectedCapitalCost) < tolerance);
             assertTrue("Check Type 3 annualized cost", Math.abs(annualizedCapitalCost - expectedAnnualizedCCost) < tolerance);
             assertTrue("Check Type 3 computed CPT", Math.abs(computedCPT - expectedComputedCPT) < tolerance);
             
             double capRecFactor1=type3.getCapRecFactor(15, 0.2);
             double capRecFactor2=type3.getCapRecFactor(0, 0.2);
             double expCapRecFactor1=0.2;
             double expCapRecFactor2=0.2;
             
             assertTrue("Check Type 3 capital recovery factor with equipment life=15 ",  (capRecFactor1 - expCapRecFactor1) < tolerance);
             assertTrue("Check Type 3 capital recovery factor with equipment life=0 ",  (capRecFactor2 - expCapRecFactor2) < tolerance);
             System.out.println("end type 3 test --------------------");
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
    
    public void testEquationType3large() throws Exception {
        Type3CostEquation type3 = new Type3CostEquation(discountRate);
        
        type3.setUpTest(reducedEmission, 15, 0.2, bestMeasureEffRecord, 1028000.0);
        
        try {
             double operatingCostResult = type3.getOperationMaintenanceCost();
             double expectedOperatingCost = 64660535.25;
             
             double annualCost = type3.getAnnualCost();
             double expectdAnnualCost=76245757.67;
             
             double capitalCost = type3.getCapitalCost();
             double expectedCapitalCost=105517209.6;
             
             double annualizedCapitalCost=type3.getAnnualizedCapitalCost();
             double expectedAnnualizedCCost=11585222.43;
             
             double computedCPT=type3.getComputedCPT();
             double expectedComputedCPT=76245.76;
            
             assertTrue("Check Type 3 operating and maintenance cost", (operatingCostResult - expectedOperatingCost) < tolerance);
             assertTrue("Check Type 3 annual cost", (annualCost - expectdAnnualCost) < tolerance);
             assertTrue("Check Type 3 capital cost", (capitalCost - expectedCapitalCost) < tolerance);
             assertTrue("Check Type 3 annualized cost", (annualizedCapitalCost - expectedAnnualizedCCost) < tolerance);
             assertTrue("Check Type 3 computed CPT", (computedCPT - expectedComputedCPT) < tolerance);
             
             System.out.println("end type 3 large test --------------------");
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
}
