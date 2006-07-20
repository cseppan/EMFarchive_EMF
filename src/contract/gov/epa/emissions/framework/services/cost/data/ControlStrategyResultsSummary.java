package gov.epa.emissions.framework.services.cost.data;

import java.text.SimpleDateFormat;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResult;

public class ControlStrategyResultsSummary {

    private StrategyResult[] strategyResults;
    
    SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    private User user;
    
    public ControlStrategyResultsSummary(StrategyResult[] strategyResults){
        this.strategyResults = strategyResults;
    }

    public float getStrategyTotalCost() {
        float totalCost = 0;
        
        for (int i = 0; i < strategyResults.length; i++)
            totalCost += strategyResults[i].getTotalCost();
        
        return totalCost;
    }

    public float getStrategyTotalReduction() {
        float totalReduction = 0;
        
        for (int i = 0; i < strategyResults.length; i++)
            totalReduction += strategyResults[i].getTotalReduction();
        
        return totalReduction;
    }
    
    public String getStartTime() {
        int earliestStartIndex = 0;
        long earliestStartTime = strategyResults[earliestStartIndex].getStartTime().getTime();
        
        for (int i = 0; i < strategyResults.length; i++) {
            long laterTime = strategyResults[i].getStartTime().getTime();
            if (laterTime < earliestStartTime) {
                earliestStartTime = laterTime;
                earliestStartIndex = i;
            }
        }
            
        return dateFormatter.format(strategyResults[earliestStartIndex].getStartTime());
    }
    
    public String getCompletionTime() {
        int latestCompletionIndex = strategyResults.length - 1;
        long latestCompletionTime = strategyResults[latestCompletionIndex].getStartTime().getTime();
        
        for (int i = 0; i < strategyResults.length; i++) {
            long laterTime = strategyResults[i].getCompletionTime().getTime();
            if (laterTime > latestCompletionTime) {
                latestCompletionTime = laterTime;
                latestCompletionIndex = i;
            }
        }
        
        return dateFormatter.format(strategyResults[latestCompletionIndex].getCompletionTime());
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public User getUser() {
        return this.user;
    }
    
    public String getRunStatus() {
        String runStatus = "";
        
        for (int i = 0; i < strategyResults.length; i++) {
            String status = strategyResults[i].getRunStatus();
            if (status.indexOf("Failed") >= 0 || status.indexOf("failed") >= 0)
                runStatus += status + System.getProperty("line.separator");
        }
        
        return runStatus.length() == 0 ? "Completed successfully." : "Failed at: " + runStatus;
    }
    
}
