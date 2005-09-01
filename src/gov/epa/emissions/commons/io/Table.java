package gov.epa.emissions.commons.io;

public class Table {
    private String tableName;

    private String tableType;

    public Table(String tableType, String tablename) {
        this.tableName = tablename;
        this.tableType = tableType;
    }

    public Table() {
    }

    public String getTableName() {
        return tableName;
    }

    public String getTableType() {
        return tableType;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setTableType(String tableType) {
        this.tableType = tableType;
    }

}
