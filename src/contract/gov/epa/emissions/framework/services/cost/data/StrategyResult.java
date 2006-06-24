package gov.epa.emissions.framework.services.cost.data;

import java.io.Serializable;
import java.util.regex.Pattern;

public class StrategyResult implements Serializable {
    
    private int id;
    
    private String table;

    private String[] cols;
    
    private String type;
    
    private int datasetId;
    
    private String datasetName;
    
    private double totalCost;
    
    private double totalReduction;
    
    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public int getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(int datasetId) {
        this.datasetId = datasetId;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public StrategyResult() {
        //
    }

    public String[] getCols() {
        return cols;
    }

    public void setCols(String[] cols) {
        this.cols = cols;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    public String getColsList() {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < cols.length; i++) {
            buf.append(cols[i]);
            if ((i + 1) < cols.length)
                buf.append(", ");
        }

        return buf.toString();
    }

    public void setColsList(String colsList) {
        Pattern p = Pattern.compile(", ");
        cols = p.split(colsList);
    }

    public double getTotalReduction() {
        return totalReduction;
    }

    public void setTotalReduction(double totalReduction) {
        this.totalReduction = totalReduction;
    }

}
