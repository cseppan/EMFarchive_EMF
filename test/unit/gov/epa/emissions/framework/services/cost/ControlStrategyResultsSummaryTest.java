package gov.epa.emissions.framework.services.cost;

import java.text.SimpleDateFormat;
import java.util.Date;

import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResult;
import gov.epa.emissions.framework.services.cost.data.ControlStrategyResultsSummary;
import junit.framework.TestCase;

public class ControlStrategyResultsSummaryTest extends TestCase {
    
    SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    public void testShouldGiveCorrectTotalValues() {
        StrategyResult result1 = createStrategyResult(new Date(10000), "Created for input dataset: test");
        StrategyResult result2 = createStrategyResult(new Date(11000), "Succeeded. Three");
        StrategyResult result3 = createStrategyResult(new Date(10000), "Succeeded. Three");
        StrategyResult result4 = createStrategyResult(new Date(12000), "Succeeded. Four");
        StrategyResult result5 = createStrategyResult(new Date(500), "Succeeded. Five");
        
        StrategyResult[] results = new StrategyResult[] {
                result1, result2, result3, result4, result5
        };
        
        ControlStrategyResultsSummary summary = new ControlStrategyResultsSummary(results);
        assertEquals(new Float(500), new Float(summary.getStrategyTotalCost()));
        assertEquals(new Float(5000), new Float(summary.getStrategyTotalReduction()));
        assertEquals("Completed successfully.", summary.getRunStatus());
        assertEquals(dateFormatter.format(new Date(500)).toString(), summary.getStartTime().toString());
        assertEquals(dateFormatter.format(new Date(12000)).toString(), summary.getCompletionTime().toString());
    }

    public void testShouldGiveFailedRunStatus() {
        StrategyResult result1 = createStrategyResult(new Date(10000), "Created for input dataset: test");
        StrategyResult result2 = createStrategyResult(new Date(11000), "Succeeded. Three");
        StrategyResult result3 = createStrategyResult(new Date(10000), "Failed. Three");
        StrategyResult result4 = createStrategyResult(new Date(10000), "Failed. Four");
        
        StrategyResult[] results = new StrategyResult[] {result1, result2, result3, result4};
        
        ControlStrategyResultsSummary summary = new ControlStrategyResultsSummary(results);
        assertEquals("Failed at: Failed. Three" + System.getProperty("line.separator") +
                "Failed. Four" + System.getProperty("line.separator"), summary.getRunStatus());
    }
    
    private StrategyResult createStrategyResult(Date date, String status) {
        StrategyResult result = new StrategyResult();
        result.setStartTime(date);
        result.setCompletionTime(date);
        result.setTotalCost(100);
        result.setTotalReduction(1000);
        result.setRunStatus(status);
        
        return result;
    }
}
