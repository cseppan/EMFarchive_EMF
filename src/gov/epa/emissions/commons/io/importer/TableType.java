package gov.epa.emissions.commons.io.importer;

public class TableType {

    private String datasetType;

    private String summaryType;

    private String[] baseTypes;

    public TableType(String datasetType, String[] baseTypes, String summaryType) {
        this.datasetType = datasetType;
        this.baseTypes = baseTypes;
        this.summaryType = summaryType;
    }

    public String getDatasetType() {
        return datasetType;
    }

    public String summaryType() {
        return summaryType;
    }

    public String[] baseTypes() {
        return baseTypes;
    }

}