package gov.epa.emissions.commons.io;

public class Table {
    private String tableName;

    private String tableType;

    public Table(String tableType, String tablename) {
        this.tableName = tablename;
        this.tableType = tableType;
    }

    public String tableName() {
        return tableName;
    }

    public String tableType() {
        return tableType;
    }

}
