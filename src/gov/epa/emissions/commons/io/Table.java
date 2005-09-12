package gov.epa.emissions.commons.io;

import java.io.Serializable;

public class Table implements Serializable {
    private String tableName;

    private String tableType;

    public Table(String tableType, String tablename) {
        this.tableName = tablename;
        this.tableType = tableType;
    }

    public Table() {// Needed for Axis serialization i.e. transport-over-wire
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
