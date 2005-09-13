package gov.epa.emissions.commons.io.importer;

public class TableData {
    private String[] columnHeader;

    private Object[][] data;

    /**
     * @pre columnHeader != null NOT CHECKED
     * @pre data != null NOT CHECKED
     * @pre columnHeader.length = data[0].length
     */
    public TableData(String[] columnHeader, Object[][] data) {
        if (columnHeader.length != data[0].length) {
            throw new IllegalArgumentException("TableData: columnHeader.length != data[0].length");
        }
        this.columnHeader = columnHeader;
        this.data = data;
    }

    public String[] getColumnHeader() {
        return columnHeader;
    }

    public Object[][] getData() {
        return data;
    }

}
